package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ComponentSequence {

  private final @Nullable ComponentSequence parentSequence;
  private final SlotContext slotContext;
  private final SlotContext resetContext;
  private final Interpreter interpreter;
  private final ComponentConstructor componentConstructor;
  public final @Nullable MarkupNode nonTerminal;
  private @Nullable List<Object> members;

  private @Nullable List<String> bufferedTexts;
  private @Nullable ComputedStyle bufferedTextsStyle;
  private DelayedCreationHandler creationHandler;

  public final @Nullable ComputedStyle computedStyle;
  public final @Nullable ComputedStyle effectiveStyle;
  public final @Nullable ComputedStyle styleToApply;

  private @Nullable ComputedStyle commonStyle;

  public @Nullable Object onTerminal(TerminalNode node, DelayedCreationHandler creationHandler) {
    ComputedStyle style = makeChildSequence(node).styleToApply;

    Object result = null;

    if (node instanceof TextNode) {
      String text = ((TextNode) node).text;

      if (creationHandler != DelayedCreationHandler.IMMEDIATE_SENTINEL) {
        addBufferedText(text, style, creationHandler);
        return null;
      }

      result = componentConstructor.createTextNode(text);
    }

    else if (node instanceof KeyNode) {
      String key = interpreter.evaluateAsString(((KeyNode) node).key);
      result = componentConstructor.createTextNode(key);
    }

    else if (node instanceof ScoreNode) {
      ScoreNode scoreNode = (ScoreNode) node;
      String name = interpreter.evaluateAsString(scoreNode.name);
      String objective = interpreter.evaluateAsString(scoreNode.objective);
      String value = interpreter.evaluateAsStringOrNull(scoreNode.value);
      result = componentConstructor.createScoreNode(name, objective, value);
    }

    else if (node instanceof SelectorNode) {
      SelectorNode selectorNode = (SelectorNode) node;
      String selector = interpreter.evaluateAsString(selectorNode.selector);

      Object separator = null;

      if (selectorNode.separator != null) {
        List<Object> components = interpreter.interpretSubtree(
          selectorNode.separator,
          componentConstructor.getSlotContext(SlotType.SELECTOR_SEPARATOR)
        );

        separator = components.isEmpty() ? null : components.get(0);
      }

      result = componentConstructor.createSelectorNode(selector, separator);
    }

    else if (node instanceof NbtNode) {
      NbtNode nbtNode = (NbtNode) node;

      String identifier = interpreter.evaluateAsString(nbtNode.identifier);
      String path = interpreter.evaluateAsString(nbtNode.path);

      boolean interpret = false;

      if (nbtNode.interpret != null)
        interpret = interpreter.evaluateAsBoolean(nbtNode.interpret);

      Object separator = null;

      if (nbtNode.separator != null) {
        List<Object> components = interpreter.interpretSubtree(
          nbtNode.separator,
          componentConstructor.getSlotContext(SlotType.NBT_SEPARATOR)
        );

        separator = components.isEmpty() ? null : components.get(0);
      }

      switch (nbtNode.source) {
        case BLOCK:
          result = componentConstructor.createBlockNbtNode(identifier, path, interpret, separator);
          break;

        case ENTITY:
          result = componentConstructor.createEntityNbtNode(identifier, path, interpret, separator);
          break;

        case STORAGE:
          result = componentConstructor.createStorageNbtNode(identifier, path, interpret, separator);
          break;

        default:
          LoggerProvider.get().log(Level.WARNING, "Encountered unknown nbt-source: " + nbtNode.source);
      }
    }

    else if (node instanceof TranslateNode) {
      TranslateNode translateNode = (TranslateNode) node;

      String key = interpreter.evaluateAsString(translateNode.key);

      List<Object> with = new ArrayList<>();

      for (MarkupNode withNode : translateNode.with.get(interpreter)) {
        List<Object> components = interpreter.interpretSubtree(
          withNode,
          componentConstructor.getSlotContext(SlotType.TRANSLATE_WITH)
        );

        if (!components.isEmpty())
          with.add(components.get(0));
      }

      String fallback = null;

      if (translateNode.fallback != null)
        fallback = interpreter.evaluateAsStringOrNull(translateNode.fallback);

      result = componentConstructor.createTranslateNode(key, with, fallback);
    }

    else
      LoggerProvider.get().log(Level.WARNING, "Unknown terminal-node: " + node.getClass());

    if (result == null)
      result = componentConstructor.createTextNode("<error>");

    if (style != null)
      style.applyStyles(result, componentConstructor);

    addMember(result, style);

    return result;
  }

  public void possiblyUpdateCommonStyleToOnlyElement() {
    if (members == null || members.size() != 1)
      return;

    if (this.commonStyle == null) {
      this.commonStyle = this.styleToApply;
      return;
    }

    this.commonStyle.addMissing(this.styleToApply);
  }

  public @Nullable ComputedStyle getCommonStyle() {
    return commonStyle;
  }

  public void addBufferedText(String text, @Nullable ComputedStyle style, DelayedCreationHandler creationHandler) {
    this.creationHandler = creationHandler;

    if (this.bufferedTexts == null)
      this.bufferedTexts = new ArrayList<>();

    if (!Objects.equals(style, bufferedTextsStyle))
      concatAndInstantiateBufferedTexts();

    this.bufferedTexts.add(text);
    bufferedTextsStyle = style;

    if (commonStyle == null)
      commonStyle = style == null ? new ComputedStyle() : style;
  }

  private void concatAndInstantiateBufferedTexts() {
    if (bufferedTexts == null || bufferedTexts.isEmpty())
      return;

    int unstyledCount = bufferedTexts.size();

    if (this.members == null)
      this.members = new ArrayList<>();

    Object result;

    if (unstyledCount == 1)
      result = componentConstructor.createTextNode(bufferedTexts.get(0));

    else {
      StringBuilder accumulator = new StringBuilder();

      for (String unstyledText : bufferedTexts)
        accumulator.append(unstyledText);

      result = componentConstructor.createTextNode(accumulator.toString());
    }

    if (bufferedTextsStyle != null)
      bufferedTextsStyle.applyStyles(result, componentConstructor);

    if (creationHandler != DelayedCreationHandler.NONE_SENTINEL)
      creationHandler.handle(result);

    members.add(result);

    bufferedTexts.clear();
  }

  public void addMember(Object member, @Nullable ComputedStyle memberCommonStyle) {
    concatAndInstantiateBufferedTexts();

    if (this.members == null)
      this.members = new ArrayList<>();

    if (memberCommonStyle != null) {
      if (this.commonStyle == null)
        this.commonStyle = memberCommonStyle.copy();
      else
        this.commonStyle.subtractUncommonProperties(memberCommonStyle);
    }

    this.members.add(member);
  }

  public Object combine(ComponentConstructor componentConstructor) {
    concatAndInstantiateBufferedTexts();

    if (this.members == null || this.members.isEmpty())
     return componentConstructor.createTextNode("");

    if (members.size() == 1)
      return members.get(0);

    Object sequenceComponent = componentConstructor.createTextNode("");
    componentConstructor.setChildren(sequenceComponent, members);
    return sequenceComponent;
  }

  public static ComponentSequence initial(
    SlotContext slotContext,
    SlotContext resetContext,
    ComponentConstructor componentConstructor, Interpreter interpreter) {
    return new ComponentSequence(
      null, slotContext, resetContext, null,
      null, slotContext.defaultStyle, null,
      componentConstructor, interpreter
    );
  }

  public ComponentSequence makeChildSequence(MarkupNode styleProvider) {
    ComputedStyle childStyle = null;

    if (styleProvider instanceof StyledNode)
      childStyle = new ComputedStyle((StyledNode) styleProvider, interpreter);

    ComputedStyle styleToApply = childStyle;
    ComputedStyle effectiveStyle = childStyle;
    ComputedStyle inheritedStyle = this.effectiveStyle;

    if (inheritedStyle != null) {
      // Do not specify styles explicitly which are already active due to inheritance
      if (styleToApply != null)
        styleToApply = styleToApply.copy().subtractEqualStyles(inheritedStyle);

      // Add the inherited style to what's currently effective
      effectiveStyle = effectiveStyle == null ? inheritedStyle : effectiveStyle.copy().addMissing(inheritedStyle);

      // Add explicit properties to invert unwanted inherited style
      // By definition, a reset means resetting to chat-state; thus,
      // that's the context to get defaults from
      if (styleProvider != null && styleProvider.doesResetStyle) {
        ComputedStyle mask = inheritedStyle.copy().subtractEqualStyles(childStyle);

        if (styleToApply == null)
          styleToApply = new ComputedStyle();

        styleToApply = styleToApply.applyDefaults(mask, resetContext);
      }
    }

    return new ComponentSequence(this, slotContext, resetContext, styleProvider, childStyle, effectiveStyle, styleToApply, componentConstructor, interpreter);
  }

  public ComponentSequence(
    @Nullable ComponentSequence parentSequence,
    SlotContext slotContext,
    SlotContext resetContext,
    @Nullable MarkupNode nonTerminal,
    @Nullable ComputedStyle computedStyle,
    @Nullable ComputedStyle effectiveStyle,
    @Nullable ComputedStyle styleToApply,
    ComponentConstructor componentConstructor,
    Interpreter interpreter
  ) {
    this.parentSequence = parentSequence;
    this.slotContext = slotContext;
    this.resetContext = resetContext;
    this.nonTerminal = nonTerminal;
    this.members = new ArrayList<>();
    this.computedStyle = computedStyle;
    this.effectiveStyle = effectiveStyle;
    this.styleToApply = styleToApply;
    this.componentConstructor = componentConstructor;
    this.interpreter = interpreter;
  }
}
