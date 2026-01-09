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
import at.blvckbytes.component_markup.constructor.*;
import at.blvckbytes.component_markup.util.logging.GlobalLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ComponentSequence<B, C> {

  private static final String DEFAULT_MATERIAL = "minecraft:stone";

  private final SlotContext slotContext;
  private final SlotContext resetContext;
  private final MarkupInterpreter<B, C> interpreter;
  private final ComponentConstructor<B, C> componentConstructor;

  private final @Nullable MarkupNode nonTerminal;
  private final boolean isInitial;

  private @Nullable List<MemberAndStyle<B>> memberEntries;

  // Style-properties which are equal amongst all members
  // E.g.: if all members are bold, that style may be hoisted up
  private @Nullable ComputedStyle membersEqualStyle;

  // Style-properties which are common amongst all members
  // E.g.: if all members specify a color, the parent-color becomes obsolete
  private @Nullable ComputedStyle membersCommonStyle;

  private @Nullable List<String> bufferedTexts;
  private @Nullable ComputedStyle bufferedTextsStyle;
  private Consumer<B> textCreationHandler;

  private final @Nullable ComputedStyle computedStyle;
  private final ComputedStyle parentStyle;
  private final ComputedStyle selfAndParentStyle;

  public void onRaw(RawNode node) {
    ComputedStyle nodeStyle = ComputedStyle.computeFor(node, interpreter);

    // Yes, this indeed is slightly hackish... That being said, we may need to modify the style
    // of- or add various events to this finalized component later on, so to unify the algorithm,
    // let's simply accept this little inefficiency as a tradeoff for increased flexibility.
    if (componentConstructor.getComponentClass().isInstance(node.value)) {
      B builder = componentConstructor.createTextComponent("");

      //noinspection unchecked
      componentConstructor.addChildren(builder, Collections.singletonList((C) node.value));

      addMember(builder, nodeStyle);
      return;
    }

    addBufferedText(String.valueOf(node.value), nodeStyle, null);
  }

  public void onUnit(UnitNode node, @Nullable Consumer<B> creationHandler) {
    ComputedStyle nodeStyle = ComputedStyle.computeFor(node, interpreter);

    B result = null;

    if (node instanceof KeyNode) {
      String key = interpreter.evaluateAsString(((KeyNode) node).key);

      if (componentConstructor.doesSupport(ConstructorFeature.KEY_COMPONENT))
        result = componentConstructor.createKeyComponent(key);

      else {
        result = componentConstructor.createTextComponent(key);
        interpreter.getLogger().logErrorScreen(node.positionProvider, "Key-components are not supported on this platform");
      }
    }

    else if (node instanceof TranslateNode) {
      TranslateNode translateNode = (TranslateNode) node;

      String key = interpreter.evaluateAsString(translateNode.key);

      List<C> with = new ArrayList<>();

      for (MarkupNode withNode : translateNode.with.get(interpreter))
        with.add(interpreter.interpretSubtree(withNode, SlotType.SINGLE_LINE_CHAT).get(0));

      String fallback = null;

      if (translateNode.fallback != null)
        fallback = interpreter.evaluateAsStringOrNull(translateNode.fallback);

      if (componentConstructor.doesSupport(ConstructorFeature.TRANSLATE_COMPONENT))
        result = componentConstructor.createTranslateComponent(key, with, fallback);

      else {
        result = componentConstructor.createTextComponent(key);
        interpreter.getLogger().logErrorScreen(node.positionProvider, "Translate-components are not supported on this platform");
      }
    }

    else
      GlobalLogger.log(Level.WARNING, "Encountered unknown unit-node: " + node.getClass());

    if (result == null)
      result = componentConstructor.createTextComponent("<error>");

    addMember(result, nodeStyle);

    if (creationHandler != null)
      creationHandler.accept(result);
  }

  public void onText(TextNode node, @Nullable Consumer<B> creationHandler, boolean doNotBuffer) {
    ComputedStyle nodeStyle = ComputedStyle.computeFor(node, interpreter);

    if (doNotBuffer) {
      B result = componentConstructor.createTextComponent(node.textValue);

      addMember(result, nodeStyle);

      if (creationHandler != null)
        creationHandler.accept(result);

      return;
    }

    addBufferedText(node.textValue, nodeStyle, creationHandler);
  }

  private void addBufferedText(String text, @Nullable ComputedStyle style, Consumer<B> creationHandler) {
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

    B result;

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

  public B addSequence(ComponentSequence<B, C> sequence, @Nullable Runnable preFinalize) {
    CombinationResult<B> result = sequence.combineOrBubbleUpAndClearMembers(this, preFinalize);

    if (result == CombinationResult.NO_OP_SENTINEL)
      return componentConstructor.createTextComponent("");

    addMember(result.component, result.styleToApply);

    return result.component;
  }

  private void addMember(B member, @Nullable ComputedStyle memberStyle) {
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

    if (this.membersCommonStyle == null)
      this.membersCommonStyle = memberStyle == null ? new ComputedStyle() : memberStyle.copy();
    else
      this.membersCommonStyle.subtractStylesOnCommonality(memberStyle, false);

    this.memberEntries.add(new MemberAndStyle<>(member, memberStyle));
  }

  public @NotNull CombinationResult<B> combineOrBubbleUpAndClearMembers(ComponentSequence<B, C> parentSequence, @Nullable Runnable preFinalize) {
    // There's no reason to create a wrapper-component because we're neither at the root,
    // nor are there any equal member-styles which could be extracted, nor does the non-terminal
    // which initiated this sequence take any effect via style or interactivity. Bubble up
    // all elements to the parent-sequence, effectively making this instance invisible.
    if (
      !isInitial
        && this.nonTerminal == null
        && (this.membersEqualStyle == null || this.membersEqualStyle.hasNoEffect())
        && (this.computedStyle == null || !this.computedStyle.reset)
        && (textCreationHandler == null)
    ) {
      if (memberEntries != null) {
        for (MemberAndStyle<B> memberAndStyle : memberEntries)
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

      //noinspection unchecked
      return (CombinationResult<B>) CombinationResult.NO_OP_SENTINEL;
    }

    concatAndInstantiateBufferedTexts();

    if (this.memberEntries == null || this.memberEntries.isEmpty())
      return CombinationResult.empty(componentConstructor);

    B result;

    // Apply no styles, as they're all kept in the common-style
    if (memberEntries.size() == 1) {
      MemberAndStyle<B> onlyMember = memberEntries.get(0);
      result = onlyMember.member;
    }

    else {
      result = componentConstructor.createTextComponent("");

      if (preFinalize != null)
        preFinalize.run();

      List<C> members = new ArrayList<>();

      for (MemberAndStyle<B> memberEntry : memberEntries) {
        if (memberEntry.style != null) {
          memberEntry.style.subtractStylesOnEquality(membersEqualStyle, true);
          memberEntry.style.applyStyles(memberEntry.member, componentConstructor, interpreter.getLogger());
        }

        members.add(componentConstructor.finalizeComponent(memberEntry.member));
      }

      componentConstructor.addChildren(result, members);
    }

    possiblyApplyNonTerminal(result);

    ComputedStyle styleToApply;

    // If there's an equal style on all elements, its properties prevail over the container's
    styleToApply = ComputedStyle.addMissing(membersEqualStyle, computedStyle);

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

    return new CombinationResult<>(result, styleToApply);
  }

  private void possiblyApplyNonTerminal(B result) {
    ConstructorWarning.clear();

    if (nonTerminal instanceof ClickNode) {
      ClickNode clickNode = (ClickNode) nonTerminal;

      String value = interpreter.evaluateAsString(clickNode.value);

      switch (clickNode.action) {
        case COPY_TO_CLIPBOARD:
          if (!componentConstructor.doesSupport(ConstructorFeature.COPY_TO_CLIPBOARD_ACTION)) {
            interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The click-action copy-to-clipboard is not supported on this platform");
            return;
          }

          componentConstructor.setClickCopyToClipboardAction(result, value);
          return;

        case SUGGEST_COMMAND:
          if (!componentConstructor.doesSupport(ConstructorFeature.SUGGEST_COMMAND_ACTION)) {
            interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The click-action suggest-command is not supported on this platform");
            return;
          }

          componentConstructor.setClickSuggestCommandAction(result, value);
          return;

        case RUN_COMMAND:
          if (!componentConstructor.doesSupport(ConstructorFeature.RUN_COMMAND_ACTION)) {
            interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The click-action run-command is not supported on this platform");
            return;
          }

          componentConstructor.setClickRunCommandAction(result, value);
          return;

        case CHANGE_PAGE:
          if (!componentConstructor.doesSupport(ConstructorFeature.CHANGE_PAGE_ACTION)) {
            interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The click-action change-page is not supported on this platform");
            return;
          }

          componentConstructor.setClickChangePageAction(result, value);
          ConstructorWarning.logIfEmitted(ConstructorWarning.MALFORMED_PAGE_VALUE, clickNode.value.getFirstMemberPositionProvider(), interpreter.getLogger(), value);
          return;

        case OPEN_FILE:
          if (!componentConstructor.doesSupport(ConstructorFeature.OPEN_FILE_ACTION)) {
            interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The click-action open-file is not supported on this platform");
            return;
          }

          componentConstructor.setClickOpenFileAction(result, value);
          return;

        case OPEN_URL: {
          if (!componentConstructor.doesSupport(ConstructorFeature.OPEN_URL_ACTION)) {
            interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The click-action open-url is not supported on this platform");
            return;
          }

          componentConstructor.setClickOpenUrlAction(result, value);
          ConstructorWarning.logIfEmitted(ConstructorWarning.MALFORMED_URL, clickNode.value.getFirstMemberPositionProvider(), interpreter.getLogger(), value);
          return;
        }

        default:
          GlobalLogger.log(Level.WARNING, "Encountered unknown click-action: " + clickNode.action);
      }

      return;
    }

    if (nonTerminal instanceof InsertNode) {
      if (!componentConstructor.doesSupport(ConstructorFeature.INSERT_ACTION)) {
        interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The insert-action is not supported on this platform");
        return;
      }

      InsertNode insertNode = (InsertNode) nonTerminal;
      String value = interpreter.evaluateAsString(insertNode.value);
      componentConstructor.setInsertAction(result, value);
      return;
    }

    if (nonTerminal instanceof EntityHoverNode) {
      if (!componentConstructor.doesSupport(ConstructorFeature.HOVER_ENTITY_ACTION)) {
        interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The hover-entity-action is not supported on this platform");
        return;
      }

      EntityHoverNode entityHoverNode = (EntityHoverNode) nonTerminal;
      String type = interpreter.evaluateAsString(entityHoverNode.type);
      String id = interpreter.evaluateAsString(entityHoverNode.id);

      C nameComponent = entityHoverNode.name == null ? null : interpreter.interpretSubtree(entityHoverNode.name, SlotType.ENTITY_NAME).get(0);

      UUID uuid;

      try {
        uuid = UUID.fromString(id);
      } catch (Throwable e) {
        interpreter.getLogger().logErrorScreen(entityHoverNode.id.getFirstMemberPositionProvider(), "Encountered malformed UUID: \"" + id + "\"");
        return;
      }

      componentConstructor.setHoverEntityAction(result, type, uuid, nameComponent);
      ConstructorWarning.logIfEmitted(ConstructorWarning.MALFORMED_ENTITY_TYPE, entityHoverNode.type.getFirstMemberPositionProvider(), interpreter.getLogger(), type);
      return;
    }

    if (nonTerminal instanceof ItemHoverNode) {
      if (!componentConstructor.doesSupport(ConstructorFeature.HOVER_ITEM_ACTION)) {
        interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The hover-item-action is not supported on this platform");
        return;
      }

      ItemHoverNode itemHoverNode = (ItemHoverNode) nonTerminal;

      String material;

      if (itemHoverNode.material != null)
        material = interpreter.evaluateAsStringOrNull(itemHoverNode.material);
      else
        material = null;

      Integer count = itemHoverNode.amount == null ? null : (int) interpreter.evaluateAsLong(itemHoverNode.amount);
      List<C> loreComponents = itemHoverNode.lore == null ? null : interpreter.interpretSubtree(itemHoverNode.lore, SlotType.ITEM_LORE);
      boolean hideProperties = itemHoverNode.hideProperties != null && interpreter.evaluateAsBoolean(itemHoverNode.hideProperties);
      C nameComponent = itemHoverNode.name == null ? null : interpreter.interpretSubtree(itemHoverNode.name, SlotType.ITEM_NAME).get(0);

      if (material == null)
        material = DEFAULT_MATERIAL;

      componentConstructor.setHoverItemAction(result, material, count, nameComponent, loreComponents, hideProperties);

      if (itemHoverNode.material != null)
        ConstructorWarning.logIfEmitted(ConstructorWarning.MALFORMED_MATERIAL, itemHoverNode.material.getFirstMemberPositionProvider(), interpreter.getLogger(), material);
      else {
        ConstructorWarning.callIfEmitted(
          ConstructorWarning.MALFORMED_MATERIAL,
          () -> GlobalLogger.log(Level.WARNING, "Encountered an invalid default material-value: \"" + DEFAULT_MATERIAL + "\"")
        );
      }

      return;
    }

    if (nonTerminal instanceof TextHoverNode) {
      if (!componentConstructor.doesSupport(ConstructorFeature.HOVER_TEXT_ACTION)) {
        interpreter.getLogger().logErrorScreen(nonTerminal.positionProvider, "The hover-text-action is not supported on this platform");
        return;
      }

      TextHoverNode textHoverNode = (TextHoverNode) nonTerminal;

      C textComponent = interpreter.interpretSubtree(textHoverNode.value, SlotType.SINGLE_LINE_CHAT).get(0);

      componentConstructor.setHoverTextAction(result, textComponent);
    }
  }

  public ComponentSequence<B, C> makeChildSequence(MarkupNode childNode) {
    ComputedStyle childParentStyle = ComputedStyle.addMissing(computedStyle, parentStyle);
    return new ComponentSequence<>(childParentStyle, childNode, false, slotContext, resetContext, interpreter);
  }

  public static <B, C> ComponentSequence<B, C> initial(
    SlotContext slotContext,
    SlotContext resetContext,
    MarkupInterpreter<B, C> interpreter
  ) {
    return new ComponentSequence<>(slotContext.defaultStyle, null, true, slotContext, resetContext, interpreter);
  }

  private ComponentSequence(
    ComputedStyle parentStyle,
    @Nullable MarkupNode nonTerminal,
    boolean isInitial,
    SlotContext slotContext,
    SlotContext resetContext,
    MarkupInterpreter<B, C> interpreter
  ) {
    this.parentStyle = parentStyle;
    this.slotContext = slotContext;
    this.resetContext = resetContext;
    this.memberEntries = new ArrayList<>();
    this.componentConstructor = interpreter.getComponentConstructor();
    this.interpreter = interpreter;
    this.nonTerminal = doesNonTerminalHaveEffect(nonTerminal) ? nonTerminal : null;

    this.isInitial = isInitial;

    // The style is also captured at this very moment
    this.computedStyle = ComputedStyle.computeFor(nonTerminal, interpreter);

    appendResetPropertiesIfApplicable(this.computedStyle, parentStyle);

    this.selfAndParentStyle = ComputedStyle.addMissing(this.computedStyle, this.parentStyle);
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
