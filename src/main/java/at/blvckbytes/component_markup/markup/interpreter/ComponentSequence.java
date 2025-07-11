package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.AchievementHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.TextHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ComponentSequence {

  private final @Nullable ComponentSequence parentSequence;
  private final SlotContext slotContext;
  private final SlotContext resetContext;
  private final Interpreter interpreter;
  private final ComponentConstructor componentConstructor;

  private final @Nullable Consumer<Object> applyKnownNonTerminal;

  private @Nullable List<Object> members;

  private @Nullable List<String> bufferedTexts;
  private @Nullable ComputedStyle bufferedTextsStyle;
  private Consumer<Object> textCreationHandler;

  private final @Nullable ComputedStyle computedStyle;
  private final @Nullable ComputedStyle effectiveStyle;
  private final @Nullable ComputedStyle styleToApply;

  private @Nullable ComputedStyle commonStyle;

  public Object onUnit(UnitNode node) {
    ComputedStyle style = makeChildSequence(node).styleToApply;

    Object result = null;

    if (node instanceof KeyNode) {
      String key = interpreter.evaluateAsString(((KeyNode) node).key);
      result = componentConstructor.createKeyNode(key);
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
      LoggerProvider.get().log(Level.WARNING, "Unknown unit-node: " + node.getClass());

    if (result == null)
      result = componentConstructor.createTextNode("<error>");

    if (style != null)
      style.applyStyles(result, componentConstructor);

    addMember(result, style);

    return result;
  }

  public void onText(TextNode node, @Nullable Consumer<Object> creationHandler, boolean doNotBuffer) {
    ComputedStyle style = makeChildSequence(node).styleToApply;

    if (doNotBuffer) {
      Object result = componentConstructor.createTextNode(node.text);

      if (style != null)
        style.applyStyles(result, componentConstructor);

      addMember(result, style);

      if (creationHandler != null)
        creationHandler.accept(result);

      return;
    }

    addBufferedText(node.text, style, creationHandler);
  }

  public void emitComponent(Object component) {
    addMember(component, null);
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

  public void addBufferedText(String text, @Nullable ComputedStyle style, Consumer<Object> creationHandler) {
    this.textCreationHandler = creationHandler;

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

    int bufferSize = bufferedTexts.size();

    Object result;

    if (bufferSize == 1)
      result = componentConstructor.createTextNode(bufferedTexts.get(0));

    else {
      StringBuilder accumulator = new StringBuilder();

      for (String unstyledText : bufferedTexts)
        accumulator.append(unstyledText);

      result = componentConstructor.createTextNode(accumulator.toString());
    }

    if (bufferedTextsStyle != null)
      bufferedTextsStyle.applyStyles(result, componentConstructor);

    bufferedTexts.clear();

    addMember(result, bufferedTextsStyle);

    bufferedTextsStyle = null;

    if (textCreationHandler != null)
      textCreationHandler.accept(result);
  }

  public Object addSequence(ComponentSequence sequence) {
    Object result = sequence.combineAndClearMembers();

    if (result == null)
      return null;

    addMember(result, sequence.commonStyle);
    return result;
  }

  private void addMember(Object member, @Nullable ComputedStyle memberCommonStyle) {
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

  public @Nullable Object combineAndClearMembers() {
    concatAndInstantiateBufferedTexts();

    if (this.members == null || this.members.isEmpty())
      return null;

    Object result;

    if (members.size() == 1)
      result = members.get(0);
    else {
      result = componentConstructor.createTextNode("");
      componentConstructor.setChildren(result, members);
    }

    if (styleToApply != null) {
      if (commonStyle != null)
        styleToApply.subtractCommonStyles(commonStyle);

      styleToApply.applyStyles(result, componentConstructor);
    }

    possiblyUpdateCommonStyleToOnlyElement();

    if (applyKnownNonTerminal != null)
      applyKnownNonTerminal.accept(result);

    members.clear();

    return result;
  }

  private @Nullable Consumer<Object> makeKnownNonTerminalClosure(MarkupNode nonTerminal) {
    if (nonTerminal == null || nonTerminal instanceof ContainerNode)
      return null;

    if (nonTerminal instanceof ClickNode) {
      ClickNode clickNode = (ClickNode) nonTerminal;

      String value = interpreter.evaluateAsString(clickNode.value);

      switch (clickNode.action) {
        case COPY_TO_CLIPBOARD:
          return result -> componentConstructor.setClickCopyToClipboardAction(result, value);

        case SUGGEST_COMMAND:
          return result -> componentConstructor.setClickSuggestCommandAction(result, value);

        case RUN_COMMAND:
          return result -> componentConstructor.setClickRunCommandAction(result, value);

        case CHANGE_PAGE:
          return result -> componentConstructor.setClickChangePageAction(result, value);

        case OPEN_FILE:
          return result -> componentConstructor.setClickOpenFileAction(result, value);

        case OPEN_URL: {
          String urlValue = interpreter.evaluateAsString(clickNode.value);

          try {
            URI uri = URI.create(urlValue);
            return result -> componentConstructor.setClickOpenUrlAction(result, uri);
          } catch (Throwable e) {
            // TODO: Provide better message
            LoggerProvider.get().log(Level.WARNING, "Encountered invalid open-url value: " + urlValue);
          }

          return null;
        }

        default:
          LoggerProvider.get().log(Level.WARNING, "Encountered unknown click-action: " + clickNode.action);
      }

      return null;
    }

    if (nonTerminal instanceof InsertNode) {
      InsertNode insertNode = (InsertNode) nonTerminal;
      String value = interpreter.evaluateAsString(insertNode.value);
      return result -> componentConstructor.setInsertAction(result, value);
    }

    if (nonTerminal instanceof AchievementHoverNode) {
      AchievementHoverNode achievementHoverNode = (AchievementHoverNode) nonTerminal;
      String value = interpreter.evaluateAsString(achievementHoverNode.value);
      return result -> componentConstructor.setHoverAchievementAction(result, value);
    }

    if (nonTerminal instanceof EntityHoverNode) {
      EntityHoverNode entityHoverNode = (EntityHoverNode) nonTerminal;
      String type = interpreter.evaluateAsString(entityHoverNode.type);
      String id = interpreter.evaluateAsString(entityHoverNode.id);

      Object name;

      if (entityHoverNode.name != null) {
        List<Object> components = interpreter.interpretSubtree(
          entityHoverNode.name,
          componentConstructor.getSlotContext(SlotType.ENTITY_NAME)
        );

        name = components.isEmpty() ? null : components.get(0);
      }
      else
        name = null;

      try {
        UUID uuid = UUID.fromString(id);
        return result -> componentConstructor.setHoverEntityAction(result, type, uuid, name);
      } catch (Throwable e) {
        // TODO: Provide better message
        LoggerProvider.get().log(Level.WARNING, "Encountered invalid hover-entity uuid: " + id);
      }

      return null;
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

      Object name;

      if (itemHoverNode.name != null) {
        List<Object> components = interpreter.interpretSubtree(
          itemHoverNode.name,
          componentConstructor.getSlotContext(SlotType.ITEM_NAME)
        );

        name = components.isEmpty() ? null : components.get(0);
      } else
        name = null;

      List<Object> lore;

      if (itemHoverNode.lore != null) {
        lore = interpreter.interpretSubtree(
          itemHoverNode.lore,
          componentConstructor.getSlotContext(SlotType.ITEM_LORE)
        );
      } else
        lore = null;

      boolean hideProperties;

      if (itemHoverNode.hideProperties != null)
        hideProperties = interpreter.evaluateAsBoolean(itemHoverNode.hideProperties);
      else
        hideProperties = false;

      return result -> componentConstructor.setHoverItemAction(result, material, count, name, lore, hideProperties);
    }

    if (nonTerminal instanceof TextHoverNode) {
      TextHoverNode textHoverNode = (TextHoverNode) nonTerminal;

      List<Object> components = interpreter.interpretSubtree(
        textHoverNode.value,
        componentConstructor.getSlotContext(SlotType.TEXT_TOOLTIP)
      );

      if (!components.isEmpty())
        return result -> componentConstructor.setHoverTextAction(result, components.get(0));
    }

    return null;
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
      if (childStyle != null && childStyle.reset) {
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
    this.members = new ArrayList<>();
    this.computedStyle = computedStyle;
    this.effectiveStyle = effectiveStyle;
    this.styleToApply = styleToApply;
    this.componentConstructor = componentConstructor;
    this.interpreter = interpreter;

    // Captures the required environment-variable values at the time of entering this tag
    // and also makes applying this exact same non-terminal again reusable.
    this.applyKnownNonTerminal = makeKnownNonTerminalClosure(nonTerminal);
  }
}
