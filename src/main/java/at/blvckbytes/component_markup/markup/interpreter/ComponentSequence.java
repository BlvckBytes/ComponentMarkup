/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.TextHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.platform.*;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ComponentSequence {

  private final @Nullable PlatformEntity recipient;
  private final SlotContext slotContext;
  private final SlotContext resetContext;
  private final MarkupInterpreter interpreter;
  private final ComponentConstructor componentConstructor;

  private final @Nullable MarkupNode nonTerminal;
  private final boolean isInitial;

  private @Nullable List<MemberAndStyle> memberEntries;

  // Style-properties which are equal amongst all members
  // E.g.: if all members are bold, that style may be hoisted up
  private @Nullable ComputedStyle membersEqualStyle;

  // Style-properties which are common amongst all members
  // E.g.: if all members specify a color, the parent-color becomes obsolete
  private @Nullable ComputedStyle membersCommonStyle;

  private @Nullable List<String> bufferedTexts;
  private @Nullable ComputedStyle bufferedTextsStyle;
  private Consumer<Object> textCreationHandler;

  private final @Nullable ComputedStyle computedStyle;
  private final ComputedStyle parentStyle;
  private final ComputedStyle selfAndParentStyle;

  private @Nullable EnumMap<MembersSlot, AddressTree> deferredAddresses;

  private void handleDeferredNode(
    DeferredNode<?> deferredNode,
    @Nullable ComputedStyle nodeStyle,
    @Nullable Consumer<Object> creationHandler
  ) {
    RendererParameter parameter;

    try {
      parameter = deferredNode.createParameter(interpreter);
    } catch (Throwable e) {
      for (String line : ErrorScreen.make(deferredNode.positionProvider, "An error occurred while trying to create the deferred component's parameter"))
        LoggerProvider.log(Level.WARNING, line, false);

      LoggerProvider.log(Level.WARNING, "This was the underlying error", e);
      return;
    }

    if (this.recipient == null) {
      Object deferredComponent = componentConstructor.createDeferredComponent(
        deferredNode,
        parameter,
        interpreter.getEnvironment().snapshot(),
        slotContext
      );

      addMember(deferredComponent, nodeStyle);

      if (creationHandler != null)
        creationHandler.accept(deferredComponent);

      return;
    }

    List<Object> renderedComponents;

    try {
      //noinspection unchecked
      renderedComponents = ((DeferredNode<RendererParameter>) deferredNode).renderComponent(
        parameter,
        componentConstructor,
        interpreter.getEnvironment(),
        slotContext,
        this.recipient
      );
    } catch (Throwable e) {
      for (String line : ErrorScreen.make(deferredNode.positionProvider, "An error occurred while trying to render the deferred component"))
        LoggerProvider.log(Level.WARNING, line, false);

      LoggerProvider.log(Level.WARNING, "This was the underlying error", e);
      return;
    }

    if (renderedComponents == null)
      return;

    for (Object renderedComponent : renderedComponents) {
      addMember(renderedComponent, nodeStyle);

      if (creationHandler != null)
        creationHandler.accept(renderedComponent);
    }
  }

  public void onUnit(UnitNode node, @Nullable Consumer<Object> creationHandler) {
    ComputedStyle nodeStyle = ComputedStyle.computeFor(node, interpreter);

    if (node instanceof DeferredNode) {
      handleDeferredNode((DeferredNode<?>) node, nodeStyle, creationHandler);
      return;
    }

    Object result = null;

    if (node instanceof KeyNode) {
      String key = interpreter.evaluateAsString(((KeyNode) node).key);
      result = componentConstructor.createKeyComponent(key);
    }

    else if (node instanceof TranslateNode) {
      TranslateNode translateNode = (TranslateNode) node;

      String key = interpreter.evaluateAsString(translateNode.key);

      List<Object> with = new ArrayList<>();

      for (MarkupNode withNode : translateNode.with.get(interpreter)) {
        ComponentOutput withOutput = interpreter.interpretSubtree(
          withNode,
          componentConstructor.getSlotContext(SlotType.SINGLE_LINE_CHAT)
        );

        if (withOutput.unprocessedComponents.isEmpty()) {
          with.add(componentConstructor.createTextComponent(""));
          continue;
        }

        if (withOutput.deferredAddresses != null) {
          EnumMap<MembersSlot, AddressTree> first = withOutput.deferredAddresses.getForFirstIndex();

          if (first != null) {
            if (deferredAddresses == null)
              deferredAddresses = AddressTree.emptyValue();

            AddressTree.put(deferredAddresses, MembersSlot.TRANSLATE_WITH, with.size(), first);
          }
        }

        with.add(withOutput.unprocessedComponents.get(0));
      }

      String fallback = null;

      if (translateNode.fallback != null)
        fallback = interpreter.evaluateAsStringOrNull(translateNode.fallback);

      result = componentConstructor.createTranslateComponent(key, with, fallback);
    }

    else
      LoggerProvider.log(Level.WARNING, "Unknown unit-node: " + node.getClass());

    if (result == null)
      result = componentConstructor.createTextComponent("<error>");

    addMember(result, nodeStyle);

    if (creationHandler != null)
      creationHandler.accept(result);
  }

  public void onText(TextNode node, @Nullable Consumer<Object> creationHandler, boolean doNotBuffer) {
    ComputedStyle nodeStyle = ComputedStyle.computeFor(node, interpreter);

    if (doNotBuffer) {
      Object result = componentConstructor.createTextComponent(node.textValue);

      addMember(result, nodeStyle);

      if (creationHandler != null)
        creationHandler.accept(result);

      return;
    }

    addBufferedText(node.textValue, nodeStyle, creationHandler);
  }

  private Object setChildren(Object component, List<Object> children) {
    Object setMembersResult = componentConstructor.setMembers(component, MembersSlot.CHILDREN, children);

    if (setMembersResult == null) {
      LoggerProvider.log(Level.WARNING, "Could not set the children of a component");
      return component;
    }

    return setMembersResult;
  }

  private void addBufferedText(String text, @Nullable ComputedStyle style, Consumer<Object> creationHandler) {
    if (style != null) {
      // If the member resets, append all necessary properties to go back to the resetContext
      appendResetPropertiesIfApplicable(style, selfAndParentStyle);

      // Don't apply styles which are already effective in this component
      style.subtractStylesOnEquality(selfAndParentStyle, true);
    }

    if (!areStylesEffectivelyEqual(style, bufferedTextsStyle))
      concatAndInstantiateBufferedTexts();

    if (this.bufferedTexts == null)
      this.bufferedTexts = new ArrayList<>();

    this.bufferedTexts.add(text);
    bufferedTextsStyle = style;
    this.textCreationHandler = creationHandler;
  }

  private boolean areStylesEffectivelyEqual(@Nullable ComputedStyle a, @Nullable ComputedStyle b) {
    if (a == null && b == null)
      return true;

    ComputedStyle nonNull, other;

    if (a != null) {
      nonNull = a;
      other = b;
    }

    else {
      nonNull = b;
      other = null;
    }

    return nonNull.doStylesEqual(other);
  }

  private void concatAndInstantiateBufferedTexts() {
    if (bufferedTexts == null || bufferedTexts.isEmpty())
      return;

    int bufferSize = bufferedTexts.size();

    Object result;

    if (bufferSize == 1)
      result = componentConstructor.createTextComponent(bufferedTexts.get(0));

    else {
      StringBuilder accumulator = new StringBuilder();

      for (String unstyledText : bufferedTexts)
        accumulator.append(unstyledText);

      result = componentConstructor.createTextComponent(accumulator.toString());
    }

    if (textCreationHandler != null) {
      textCreationHandler.accept(result);
      textCreationHandler = null;
    }

    bufferedTexts.clear();

    addMember(result, bufferedTextsStyle);

    bufferedTextsStyle = null;
  }

  public Object addSequence(ComponentSequence sequence) {
    CombinationResult result = sequence.combineOrBubbleUpAndClearMembers(this);

    if (result == CombinationResult.NO_OP_SENTINEL)
      return componentConstructor.createTextComponent("");

    if (result.deferredAddresses != null) {
      if (deferredAddresses == null)
        deferredAddresses = AddressTree.emptyValue();

      int deferredIndex = memberEntries == null ? 0 : memberEntries.size();

      AddressTree.put(deferredAddresses, MembersSlot.CHILDREN, deferredIndex, result.deferredAddresses);
    }

    addMember(result.component, result.styleToApply);

    return result.component;
  }

  private void addMember(Object member, @Nullable ComputedStyle memberStyle) {
    concatAndInstantiateBufferedTexts();

    if (this.memberEntries == null)
      this.memberEntries = new ArrayList<>();

    if (memberStyle != null) {
      // If the member resets, append all necessary properties to go back to the resetContext
      appendResetPropertiesIfApplicable(memberStyle, selfAndParentStyle);

      // Don't apply styles which are already effective in this component
      memberStyle.subtractStylesOnEquality(selfAndParentStyle, true);
    }

    if (this.membersEqualStyle == null) {
      this.membersEqualStyle = memberStyle == null ? new ComputedStyle() : memberStyle.copy();
    } else
      this.membersEqualStyle.subtractStylesOnEquality(memberStyle, false);

    if (this.membersCommonStyle == null) {
      this.membersCommonStyle = memberStyle == null ? new ComputedStyle() : memberStyle.copy();
    } else
      this.membersCommonStyle.subtractStylesOnCommonality(memberStyle, false);

    if (member instanceof DeferredComponent) {
      int deferredIndex = memberEntries.size();

      if (deferredAddresses == null)
        deferredAddresses = AddressTree.emptyValue();

      AddressTree.put(deferredAddresses, MembersSlot.CHILDREN, deferredIndex, null);
    }

    this.memberEntries.add(new MemberAndStyle(member, memberStyle));
  }

  public @NotNull CombinationResult combineOrBubbleUpAndClearMembers(ComponentSequence parentSequence) {
    // There's no reason to create a wrapper-component because we're neither at the root,
    // nor are there any equal member-styles which could be extracted, nor does the non-terminal
    // which initiated this sequence take any effect via style or interactivity. Bubble up
    // all elements to the parent-sequence, effectively making this instance invisible.
    if (
      !isInitial
        && this.nonTerminal == null
        && (this.membersEqualStyle == null || !this.membersEqualStyle.hasEffect())
        && (this.computedStyle == null || !this.computedStyle.reset)
        && (textCreationHandler == null)
    ) {
      if (memberEntries != null) {
        for (MemberAndStyle memberAndStyle : memberEntries)
          parentSequence.addMember(memberAndStyle.member, ComputedStyle.addMissing(memberAndStyle.style, this.computedStyle));

        this.memberEntries.clear();
      }

      if (bufferedTexts != null) {
        this.bufferedTextsStyle = ComputedStyle.addMissing(this.bufferedTextsStyle, this.computedStyle);

        for (String bufferedText : bufferedTexts)
          parentSequence.addBufferedText(bufferedText, bufferedTextsStyle, textCreationHandler);

        this.bufferedTexts.clear();
        this.bufferedTextsStyle = null;
        this.textCreationHandler = null;
      }

      this.membersEqualStyle = null;
      this.membersCommonStyle = null;
      this.deferredAddresses = null;

      return CombinationResult.NO_OP_SENTINEL;
    }

    concatAndInstantiateBufferedTexts();

    if (this.memberEntries == null || this.memberEntries.isEmpty())
      return CombinationResult.empty(componentConstructor);

    Object result;

    // Apply no styles, as they're all kept in the common-style
    if (memberEntries.size() == 1) {
      MemberAndStyle onlyMember = memberEntries.get(0);
      result = onlyMember.member;
    }

    else {
      result = componentConstructor.createTextComponent("");

      List<Object> members = new ArrayList<>();

      for (MemberAndStyle memberEntry : memberEntries) {

        if (memberEntry.style != null) {
          memberEntry.style.subtractStylesOnEquality(membersEqualStyle, true);
          memberEntry.style.applyStyles(memberEntry.member, componentConstructor);
        }

        members.add(memberEntry.member);
      }

      result = setChildren(result, members);
    }

    possiblyApplyNonTerminal(result, parentSequence == null ? this : parentSequence);

    ComputedStyle styleToApply;

    // If there's an equal style on all elements, its properties prevail over the container's
    if (this.membersEqualStyle != null) {
      styleToApply = this.membersEqualStyle;
      styleToApply.addMissing(this.computedStyle);
    } else
      styleToApply = this.computedStyle;

    if (styleToApply != null) {
      if (membersCommonStyle != null) {
        membersCommonStyle.subtractStylesOnEquality(membersEqualStyle, true);
        styleToApply.subtractStylesOnCommonality(membersCommonStyle, true);
      }

      styleToApply.subtractStylesOnEquality(this.parentStyle, true);
    }

    this.memberEntries.clear();
    this.membersEqualStyle = null;
    this.membersCommonStyle = null;

    CombinationResult combinationResult = new CombinationResult(result, styleToApply, deferredAddresses);

    this.deferredAddresses = null;

    return combinationResult;
  }

  private void possiblyApplyNonTerminal(Object result, ComponentSequence addressHolder) {
    if (nonTerminal instanceof ClickNode) {
      ClickNode clickNode = (ClickNode) nonTerminal;

      String value = interpreter.evaluateAsString(clickNode.value);

      switch (clickNode.action) {
        case COPY_TO_CLIPBOARD:
          componentConstructor.setClickCopyToClipboardAction(result, value);
          return;

        case SUGGEST_COMMAND:
          componentConstructor.setClickSuggestCommandAction(result, value);
          return;

        case RUN_COMMAND:
          componentConstructor.setClickRunCommandAction(result, value);
          return;

        case CHANGE_PAGE:
          componentConstructor.setClickChangePageAction(result, value);
          return;

        case OPEN_FILE:
          componentConstructor.setClickOpenFileAction(result, value);
          return;

        case OPEN_URL: {
          try {
            componentConstructor.setClickOpenUrlAction(result, value);
          } catch (Throwable e) {
            for (String line : ErrorScreen.make(clickNode.value.getFirstMemberPositionProvider(), "Encountered malformed URL \"" + value + "\": " + e.getMessage()))
              LoggerProvider.log(Level.WARNING, line, false);

          }

          return;
        }

        default:
          LoggerProvider.log(Level.WARNING, "Encountered unknown click-action: " + clickNode.action);
      }

      return;
    }

    if (nonTerminal instanceof InsertNode) {
      InsertNode insertNode = (InsertNode) nonTerminal;
      String value = interpreter.evaluateAsString(insertNode.value);
      componentConstructor.setInsertAction(result, value);
      return;
    }

    if (nonTerminal instanceof EntityHoverNode) {
      EntityHoverNode entityHoverNode = (EntityHoverNode) nonTerminal;
      String type = interpreter.evaluateAsString(entityHoverNode.type);
      String id = interpreter.evaluateAsString(entityHoverNode.id);

      ComponentOutput nameOutput;

      if (entityHoverNode.name != null) {
        nameOutput = interpreter.interpretSubtree(
          entityHoverNode.name,
          componentConstructor.getSlotContext(SlotType.ENTITY_NAME)
        );
      }
      else
        nameOutput = null;

      UUID uuid;

      try {
        uuid = UUID.fromString(id);
      } catch (Throwable e) {
        for (String line : ErrorScreen.make(entityHoverNode.id.getFirstMemberPositionProvider(), "Encountered malformed UUID: \"" + id + "\""))
          LoggerProvider.log(Level.WARNING, line, false);

        return;
      }

      Object nameComponent = null;

      if (nameOutput != null && !nameOutput.unprocessedComponents.isEmpty()) {
        nameComponent = nameOutput.unprocessedComponents.get(0);

        if (nameOutput.deferredAddresses != null) {
          EnumMap<MembersSlot, AddressTree> first = nameOutput.deferredAddresses.getForFirstIndex();

          if (first != null) {
            if (addressHolder.deferredAddresses == null)
              addressHolder.deferredAddresses = AddressTree.emptyValue();

            addressHolder.deferredAddresses.put(MembersSlot.HOVER_ENTITY_NAME, AddressTree.singleton(first));
          }
        }
      }

      componentConstructor.setHoverEntityAction(result, type, uuid, nameComponent);
    }

    if (nonTerminal instanceof ItemHoverNode) {
      ItemHoverNode itemHoverNode = (ItemHoverNode) nonTerminal;

      String material;

      if (itemHoverNode.material != null)
        material = interpreter.evaluateAsStringOrNull(itemHoverNode.material);
      else
        material = null;

      Integer count;

      if (itemHoverNode.amount != null)
        count = (int) interpreter.evaluateAsLong(itemHoverNode.amount);
      else
        count = null;

      ComponentOutput nameOutput;

      if (itemHoverNode.name != null) {
        nameOutput = interpreter.interpretSubtree(
          itemHoverNode.name,
          componentConstructor.getSlotContext(SlotType.ITEM_NAME)
        );
      } else
        nameOutput = null;

      ComponentOutput loreOutput;

      if (itemHoverNode.lore != null) {
        loreOutput = interpreter.interpretSubtree(
          itemHoverNode.lore,
          componentConstructor.getSlotContext(SlotType.ITEM_LORE)
        );
      } else
        loreOutput = null;

      boolean hideProperties;

      if (itemHoverNode.hideProperties != null)
        hideProperties = interpreter.evaluateAsBoolean(itemHoverNode.hideProperties);
      else
        hideProperties = false;

      Object nameComponent = null;

      if (nameOutput != null && !nameOutput.unprocessedComponents.isEmpty()) {
        nameComponent = nameOutput.unprocessedComponents.get(0);

        if (nameOutput.deferredAddresses != null) {
          EnumMap<MembersSlot, AddressTree> first = nameOutput.deferredAddresses.getForFirstIndex();

          if (first != null) {
            if (addressHolder.deferredAddresses == null)
              addressHolder.deferredAddresses = AddressTree.emptyValue();

            addressHolder.deferredAddresses.put(MembersSlot.HOVER_ITEM_NAME, AddressTree.singleton(first));
          }
        }
      }

      List<Object> loreComponents = null;

      if (loreOutput != null && !loreOutput.unprocessedComponents.isEmpty()) {
        loreComponents = loreOutput.unprocessedComponents;

        if (loreOutput.deferredAddresses != null) {
          if (addressHolder.deferredAddresses == null)
            addressHolder.deferredAddresses = AddressTree.emptyValue();

          addressHolder.deferredAddresses.put(MembersSlot.HOVER_ITEM_LORE, loreOutput.deferredAddresses);
        }
      }

      componentConstructor.setHoverItemAction(result, material, count, nameComponent, loreComponents, hideProperties);
    }

    if (nonTerminal instanceof TextHoverNode) {
      TextHoverNode textHoverNode = (TextHoverNode) nonTerminal;

      ComponentOutput textOutput = interpreter.interpretSubtree(
        textHoverNode.value,
        componentConstructor.getSlotContext(SlotType.SINGLE_LINE_CHAT)
      );

      if (textOutput.unprocessedComponents.isEmpty())
        return;

      Object textComponent = textOutput.unprocessedComponents.get(0);

      if (textOutput.deferredAddresses != null) {
        if (addressHolder.deferredAddresses == null)
          addressHolder.deferredAddresses = AddressTree.emptyValue();

        addressHolder.deferredAddresses.put(MembersSlot.HOVER_TEXT_VALUE, textOutput.deferredAddresses);
      }

      componentConstructor.setHoverTextAction(result, textComponent);
    }
  }

  public ComponentSequence makeChildSequence(MarkupNode childNode) {
    ComputedStyle childParentStyle = this.computedStyle;

    if (childParentStyle != null)
      childParentStyle.addMissing(this.parentStyle);
    else
      childParentStyle = this.parentStyle;

    return new ComponentSequence(recipient, childParentStyle, childNode, false, slotContext, resetContext, componentConstructor, interpreter);
  }

  public static ComponentSequence initial(
    @Nullable PlatformEntity recipient,
    SlotContext slotContext,
    SlotContext resetContext,
    ComponentConstructor componentConstructor,
    MarkupInterpreter interpreter
  ) {
    return new ComponentSequence(recipient, slotContext.defaultStyle, null, true, slotContext, resetContext, componentConstructor, interpreter);
  }

  private ComponentSequence(
    @Nullable PlatformEntity recipient,
    ComputedStyle parentStyle,
    @Nullable MarkupNode nonTerminal,
    boolean isInitial,
    SlotContext slotContext,
    SlotContext resetContext,
    ComponentConstructor componentConstructor,
    MarkupInterpreter interpreter
  ) {
    this.recipient = recipient;
    this.parentStyle = parentStyle;
    this.slotContext = slotContext;
    this.resetContext = resetContext;
    this.memberEntries = new ArrayList<>();
    this.componentConstructor = componentConstructor;
    this.interpreter = interpreter;
    this.nonTerminal = doesNonTerminalHaveEffect(nonTerminal) ? nonTerminal : null;

    this.isInitial = isInitial;

    // The style is also captured at this very moment
    this.computedStyle = ComputedStyle.computeFor(nonTerminal, interpreter);

    appendResetPropertiesIfApplicable(this.computedStyle, parentStyle);

    ComputedStyle selfAndParentStyle = this.computedStyle;

    if (selfAndParentStyle != null)
      selfAndParentStyle.addMissing(parentStyle);
    else
      selfAndParentStyle = parentStyle;

    this.selfAndParentStyle = selfAndParentStyle;
  }

  private void appendResetPropertiesIfApplicable(@Nullable ComputedStyle style, @Nullable ComputedStyle parentStyle) {
    // Add explicit properties to invert unwanted inherited style
    if (parentStyle != null && style != null && style.reset) {
      style.addMissingDefaults(parentStyle, resetContext);
      style.reset = false;
    }
  }

  private boolean doesNonTerminalHaveEffect(MarkupNode nonTerminal) {
    if (nonTerminal == null || nonTerminal instanceof ContainerNode)
      return false;

    if (nonTerminal instanceof ClickNode)
      return true;

    if (nonTerminal instanceof InsertNode)
      return true;

    if (nonTerminal instanceof EntityHoverNode)
      return true;

    if (nonTerminal instanceof ItemHoverNode)
      return true;

    return nonTerminal instanceof TextHoverNode;
  }
}
