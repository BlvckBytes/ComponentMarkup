package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.TextHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.util.CachedSupplier;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ComponentSequence {

  @FunctionalInterface
  private interface NonTerminalClosure {
    void apply(Object component, CachedSupplier<EnumMap<MembersSlot, AddressTree>> addressTree);
  }

  private final @Nullable Object recipient;
  private final SlotContext slotContext;
  private final SlotContext resetContext;
  private final Interpreter interpreter;
  private final ComponentConstructor componentConstructor;

  private final @Nullable NonTerminalClosure applyKnownNonTerminal;

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

  public void onUnit(UnitNode node, @Nullable Consumer<Object> creationHandler) {
    ComputedStyle nodeStyle = ComputedStyle.computeFor(node, interpreter);

    Object result = null;

    if (node instanceof KeyNode) {
      String key = interpreter.evaluateAsString(((KeyNode) node).key);
      result = componentConstructor.createKeyComponent(key);
    }

    else if (node instanceof DeferredNode) {
      DeferredNode<?> deferredNode = (DeferredNode<?>) node;
      RendererParameter parameter = deferredNode.createParameter(interpreter);

      if (this.recipient != null) {
        //noinspection unchecked
        List<Object> renderedComponents = ((DeferredNode<RendererParameter>) deferredNode).renderComponent(
          parameter,
          componentConstructor,
          interpreter.getEnvironment(),
          slotContext,
          this.recipient
        );

        if (renderedComponents == null)
          return;

        for (Object renderedComponent : renderedComponents) {
          addMember(renderedComponent, nodeStyle);

          if (creationHandler != null)
            creationHandler.accept(renderedComponent);
        }

        return;
      }

      result = componentConstructor.createDeferredComponent(
        deferredNode,
        parameter,
        interpreter.getEnvironment().snapshot(),
        slotContext
      );
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
      Object result = componentConstructor.createTextComponent(node.text);

      addMember(result, nodeStyle);

      if (creationHandler != null)
        creationHandler.accept(result);

      return;
    }

    addBufferedText(node.text, nodeStyle, creationHandler);
  }

  private Object setChildren(Object component, List<Object> children) {
    Object setMembersResult = componentConstructor.setMembers(component, MembersSlot.CHILDREN, children);

    if (setMembersResult == null) {
      LoggerProvider.log(Level.WARNING, "Could not set the children of a component");
      return component;
    }

    return setMembersResult;
  }

  public void emitComponent(Object component) {
    addMember(component, null);
  }

  private void addBufferedText(String text, @Nullable ComputedStyle style, Consumer<Object> creationHandler) {
    this.textCreationHandler = creationHandler;

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

    bufferedTexts.clear();

    addMember(result, bufferedTextsStyle);

    bufferedTextsStyle = null;

    if (textCreationHandler != null)
      textCreationHandler.accept(result);
  }

  public Object addSequence(ComponentSequence sequence) {
    CombinationResult result = sequence.combineAndClearMembers(() -> {
      if (deferredAddresses == null)
        deferredAddresses = AddressTree.emptyValue();

      return deferredAddresses;
    });

    if (result == null) {
      Object emptyComponent = componentConstructor.createTextComponent("");
      addMember(emptyComponent, null);
      return emptyComponent;
    }

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

  public @Nullable CombinationResult combineAndClearMembers(
    @Nullable Supplier<EnumMap<MembersSlot, AddressTree>> addressTree
  ) {
    concatAndInstantiateBufferedTexts();

    if (this.memberEntries == null || this.memberEntries.isEmpty())
      return null;

    Object result;

    // Apply no styles, as they're all kept in the common-style
    if (memberEntries.size() == 1) {
      MemberAndStyle onlyMember = memberEntries.get(0);

      if (onlyMember.style != null) {
        onlyMember.style.subtractStylesOnEquality(membersEqualStyle, true);
        onlyMember.style.applyStyles(onlyMember.member, componentConstructor);
      }

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

    if (applyKnownNonTerminal != null) {
      applyKnownNonTerminal.apply(
        result,
        new CachedSupplier<>(addressTree != null ? addressTree : () -> {
          if (deferredAddresses == null)
            deferredAddresses = AddressTree.emptyValue();

          return deferredAddresses;
        })
      );
    }

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

    if (this.memberEntries != null)
      memberEntries.clear();

    this.membersEqualStyle = null;
    this.membersCommonStyle = null;

    CombinationResult combinationResult = new CombinationResult(result, styleToApply, deferredAddresses);

    this.deferredAddresses = null;

    return combinationResult;
  }

  private @Nullable NonTerminalClosure makeKnownNonTerminalClosure(MarkupNode nonTerminal) {
    if (nonTerminal == null || nonTerminal instanceof ContainerNode)
      return null;

    if (nonTerminal instanceof ClickNode) {
      ClickNode clickNode = (ClickNode) nonTerminal;

      String value = interpreter.evaluateAsString(clickNode.value);

      switch (clickNode.action) {
        case COPY_TO_CLIPBOARD:
          return (result, addressTree) -> componentConstructor.setClickCopyToClipboardAction(result, value);

        case SUGGEST_COMMAND:
          return (result, addressTree) -> componentConstructor.setClickSuggestCommandAction(result, value);

        case RUN_COMMAND:
          return (result, addressTree) -> componentConstructor.setClickRunCommandAction(result, value);

        case CHANGE_PAGE:
          return (result, addressTree) -> componentConstructor.setClickChangePageAction(result, value);

        case OPEN_FILE:
          return (result, addressTree) -> componentConstructor.setClickOpenFileAction(result, value);

        case OPEN_URL: {
          String urlValue = interpreter.evaluateAsString(clickNode.value);

          try {
            URI uri = URI.create(urlValue);
            return (result, addressTree) -> componentConstructor.setClickOpenUrlAction(result, uri);
          } catch (Throwable e) {
            // TODO: Provide better message
            LoggerProvider.log(Level.WARNING, "Encountered invalid open-url value: " + urlValue);
          }

          return null;
        }

        default:
          LoggerProvider.log(Level.WARNING, "Encountered unknown click-action: " + clickNode.action);
      }

      return null;
    }

    if (nonTerminal instanceof InsertNode) {
      InsertNode insertNode = (InsertNode) nonTerminal;
      String value = interpreter.evaluateAsString(insertNode.value);
      return (result, addressTree) -> componentConstructor.setInsertAction(result, value);
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
        // TODO: Provide better message
        LoggerProvider.log(Level.WARNING, "Encountered invalid hover-entity uuid: " + id);
        return null;
      }

      return (result, addressTree) -> {
        Object nameComponent = null;

        if (nameOutput != null && !nameOutput.unprocessedComponents.isEmpty()) {
          nameComponent = nameOutput.unprocessedComponents.get(0);

          if (nameOutput.deferredAddresses != null) {
            EnumMap<MembersSlot, AddressTree> first = nameOutput.deferredAddresses.getForFirstIndex();

            if (first != null)
              addressTree.get().put(MembersSlot.HOVER_ENTITY_NAME, AddressTree.singleton(first));
          }
        }

        componentConstructor.setHoverEntityAction(result, type, uuid, nameComponent);
      };
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

      return (result, addressTree) -> {
        Object nameComponent = null;

        if (nameOutput != null && !nameOutput.unprocessedComponents.isEmpty()) {
          nameComponent = nameOutput.unprocessedComponents.get(0);

          if (nameOutput.deferredAddresses != null) {
            EnumMap<MembersSlot, AddressTree> first = nameOutput.deferredAddresses.getForFirstIndex();

            if (first != null)
              addressTree.get().put(MembersSlot.HOVER_ITEM_NAME, AddressTree.singleton(first));
          }
        }

        List<Object> loreComponents = null;

        if (loreOutput != null && !loreOutput.unprocessedComponents.isEmpty()) {
          loreComponents = loreOutput.unprocessedComponents;

          if (loreOutput.deferredAddresses != null)
            addressTree.get().put(MembersSlot.HOVER_ITEM_LORE, loreOutput.deferredAddresses);
        }

        componentConstructor.setHoverItemAction(result, material, count, nameComponent, loreComponents, hideProperties);
      };
    }

    if (nonTerminal instanceof TextHoverNode) {
      TextHoverNode textHoverNode = (TextHoverNode) nonTerminal;

      ComponentOutput textOutput = interpreter.interpretSubtree(
        textHoverNode.value,
        componentConstructor.getSlotContext(SlotType.SINGLE_LINE_CHAT)
      );

      if (textOutput.unprocessedComponents.isEmpty())
        return null;

      Object textComponent = textOutput.unprocessedComponents.get(0);

      return (result, addressTree) -> {
        if (textOutput.deferredAddresses != null)
          addressTree.get().put(MembersSlot.HOVER_TEXT_VALUE, textOutput.deferredAddresses);

        componentConstructor.setHoverTextAction(result, textComponent);
      };
    }

    return null;
  }

  public ComponentSequence makeChildSequence(MarkupNode childNode) {
    ComputedStyle childParentStyle = this.computedStyle;

    if (childParentStyle != null)
      childParentStyle.addMissing(this.parentStyle);
    else
      childParentStyle = this.parentStyle;

    return new ComponentSequence(recipient, childParentStyle, childNode, slotContext, resetContext, componentConstructor, interpreter);
  }

  public static ComponentSequence initial(
    @Nullable Object recipient,
    SlotContext slotContext,
    SlotContext resetContext,
    ComponentConstructor componentConstructor,
    Interpreter interpreter
  ) {
    return new ComponentSequence(recipient, slotContext.defaultStyle, null, slotContext, resetContext, componentConstructor, interpreter);
  }

  private ComponentSequence(
    @Nullable Object recipient,
    ComputedStyle parentStyle,
    @Nullable MarkupNode nonTerminal,
    SlotContext slotContext,
    SlotContext resetContext,
    ComponentConstructor componentConstructor,
    Interpreter interpreter
  ) {
    this.recipient = recipient;
    this.parentStyle = parentStyle;
    this.slotContext = slotContext;
    this.resetContext = resetContext;
    this.memberEntries = new ArrayList<>();
    this.componentConstructor = componentConstructor;
    this.interpreter = interpreter;

    // Captures the required environment-variable values at the time of entering this tag
    // and also makes applying this exact same non-terminal again reusable.
    this.applyKnownNonTerminal = makeKnownNonTerminalClosure(nonTerminal);

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
}
