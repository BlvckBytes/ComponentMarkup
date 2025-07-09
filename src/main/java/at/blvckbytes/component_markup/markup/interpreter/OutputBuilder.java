package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.markup.ast.node.control.BreakNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.markup.ast.node.hover.AchievementHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.TextHoverNode;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;

public class OutputBuilder {

  private final ComponentConstructor componentConstructor;
  private final Interpreter interpreter;
  private final SlotContext slotContext;
  private final SlotContext chatContext;
  private final @Nullable String breakString;

  private final List<Object> result;
  private final Stack<ComponentSequence> sequencesStack;

  public OutputBuilder(
    ComponentConstructor componentConstructor,
    Interpreter interpreter,
    SlotContext slotContext
  ) {
    this.componentConstructor = componentConstructor;
    this.interpreter = interpreter;
    this.slotContext = slotContext;
    this.chatContext = componentConstructor.getSlotContext(SlotType.CHAT);
    this.breakString = slotContext.breakChar == 0 ? null : String.valueOf(slotContext.breakChar);
    this.sequencesStack = new Stack<>();
    this.sequencesStack.push(ComponentSequence.initial(slotContext, chatContext, componentConstructor, interpreter));
    this.result = new ArrayList<>();
  }

  public void onBreak(BreakNode node) {
    if (breakString != null) {
      onTerminal(new TextNode(breakString, node.position), DelayedCreationHandler.NONE_SENTINEL);
      return;
    }

    List<MarkupNode> poppedNonTerminals = new ArrayList<>();

    popAllSequencesAndAddToResult(poppedNonTerminals);

    sequencesStack.push(ComponentSequence.initial(slotContext, chatContext, componentConstructor, interpreter));

    int size;

    while ((size = poppedNonTerminals.size()) != 0)
      onNonTerminalBegin(poppedNonTerminals.remove(size - 1));
  }

  public void onNonTerminalBegin(MarkupNode nonTerminal) {
    sequencesStack.push(sequencesStack.peek().makeChildSequence(nonTerminal));
  }

  public void onNonTerminalEnd() {
    if (sequencesStack.isEmpty()) {
      LoggerProvider.get().log(Level.WARNING, "Encountered unbalanced non-terminal-stack");
      return;
    }

    ComponentSequence sequence = sequencesStack.pop();
    Object item = combineSequenceAndApplyNonTerminal(sequence);
    sequencesStack.peek().addMember(item, sequence.getCommonStyle());
  }

  private Object combineSequenceAndApplyNonTerminal(ComponentSequence sequence) {
    Object sequenceComponent = sequence.combine(componentConstructor);

    if (sequence.styleToApply != null) {
      ComputedStyle commonStyle;

      if ((commonStyle = sequence.getCommonStyle()) != null)
        sequence.styleToApply.subtractCommonStyles(commonStyle);

      sequence.styleToApply.applyStyles(sequenceComponent, componentConstructor);
    }

    sequence.possiblyUpdateCommonStyleToOnlyElement();

    if (sequence.nonTerminal == null)
      return sequenceComponent;

    if (sequence.nonTerminal instanceof ClickNode) {
      ClickNode clickNode = (ClickNode) sequence.nonTerminal;

      switch (clickNode.action) {
        case COPY_TO_CLIPBOARD:
          componentConstructor.setClickCopyToClipboardAction(
            sequenceComponent,
            interpreter.evaluateAsString(clickNode.value)
          );
          break;

        case SUGGEST_COMMAND:
          componentConstructor.setClickSuggestCommandAction(
            sequenceComponent,
            interpreter.evaluateAsString(clickNode.value)
          );
          break;

        case RUN_COMMAND:
          componentConstructor.setClickRunCommandAction(
            sequenceComponent,
            interpreter.evaluateAsString(clickNode.value)
          );
          break;

        case CHANGE_PAGE:
          componentConstructor.setClickChangePageAction(
            sequenceComponent,
            (int) interpreter.evaluateAsLong(clickNode.value)
          );
          break;

        case OPEN_FILE:
          componentConstructor.setClickOpenFileAction(
            sequenceComponent,
            interpreter.evaluateAsString(clickNode.value)
          );
          break;

        case OPEN_URL: {
          String urlValue = interpreter.evaluateAsString(clickNode.value);

          try {
            URI uri = URI.create(urlValue);
            componentConstructor.setClickOpenUrlAction(sequenceComponent, uri);
          } catch (Throwable e) {
            // TODO: Provide better message
            LoggerProvider.get().log(Level.WARNING, "Encountered invalid open-url value: " + urlValue);
          }

          break;
        }

        default:
          LoggerProvider.get().log(Level.WARNING, "Encountered unknown click-action: " + clickNode.action);
      }
    }

    else if (sequence.nonTerminal instanceof InsertNode) {
      InsertNode insertNode = (InsertNode) sequence.nonTerminal;
      String value = interpreter.evaluateAsString(insertNode.value);
      componentConstructor.setInsertAction(sequenceComponent, value);
    }

    else if (sequence.nonTerminal instanceof AchievementHoverNode) {
      AchievementHoverNode achievementHoverNode = (AchievementHoverNode) sequence.nonTerminal;
      String value = interpreter.evaluateAsString(achievementHoverNode.value);
      componentConstructor.setHoverAchievementAction(sequenceComponent, value);
    }

    else if (sequence.nonTerminal instanceof EntityHoverNode) {
      EntityHoverNode entityHoverNode = (EntityHoverNode) sequence.nonTerminal;
      String type = interpreter.evaluateAsString(entityHoverNode.type);
      String id = interpreter.evaluateAsString(entityHoverNode.id);

      Object name = null;

      if (entityHoverNode.name != null) {
        List<Object> components = interpreter.interpretSubtree(
          entityHoverNode.name,
          componentConstructor.getSlotContext(SlotType.ENTITY_NAME)
        );

        name = components.isEmpty() ? null : components.get(0);
      }

      try {
        UUID uuid = UUID.fromString(id);
        componentConstructor.setHoverEntityAction(sequenceComponent, type, uuid, name);
      } catch (Throwable e) {
        // TODO: Provide better message
        LoggerProvider.get().log(Level.WARNING, "Encountered invalid hover-entity uuid: " + id);
      }
    }

    else if (sequence.nonTerminal instanceof ItemHoverNode) {
      ItemHoverNode itemHoverNode = (ItemHoverNode) sequence.nonTerminal;

      String material = null;

      if (itemHoverNode.material != null)
        material = interpreter.evaluateAsStringOrNull(itemHoverNode.material);

      Integer count = null;

      if (itemHoverNode.amount != null)
        count = (int) interpreter.evaluateAsLong(itemHoverNode.amount);

      Object name = null;

      if (itemHoverNode.name != null) {
        List<Object> components = interpreter.interpretSubtree(
          itemHoverNode.name,
          componentConstructor.getSlotContext(SlotType.ITEM_NAME)
        );

        name = components.isEmpty() ? null : components.get(0);
      }

      List<Object> lore = null;

      if (itemHoverNode.lore != null) {
        lore = interpreter.interpretSubtree(
          itemHoverNode.lore,
          componentConstructor.getSlotContext(SlotType.ITEM_LORE)
        );
      }

      boolean hideProperties = false;

      if (itemHoverNode.hideProperties != null)
        hideProperties = interpreter.evaluateAsBoolean(itemHoverNode.hideProperties);

      componentConstructor.setHoverItemAction(sequenceComponent, material, count, name, lore, hideProperties);
    }

    else if (sequence.nonTerminal instanceof TextHoverNode) {
      TextHoverNode textHoverNode = (TextHoverNode) sequence.nonTerminal;

      List<Object> components = interpreter.interpretSubtree(
        textHoverNode.value,
        componentConstructor.getSlotContext(SlotType.TEXT_TOOLTIP)
      );

      if (!components.isEmpty())
        componentConstructor.setHoverTextAction(sequenceComponent, components.get(0));
    }

    return sequenceComponent;
  }

  public @Nullable Object onTerminal(TerminalNode node, DelayedCreationHandler creationHandler) {
    return sequencesStack.peek().onTerminal(node, creationHandler);
  }

  private void popAllSequencesAndAddToResult(@Nullable List<MarkupNode> nonTerminalCollector) {
    while (!sequencesStack.isEmpty()) {
      ComponentSequence sequence = sequencesStack.pop();

      if (sequence.nonTerminal != null && nonTerminalCollector != null)
        nonTerminalCollector.add(sequence.nonTerminal);

      Object sequenceComponent = combineSequenceAndApplyNonTerminal(sequence);

      if (!sequencesStack.isEmpty()) {
        sequencesStack.peek().addMember(sequenceComponent, sequence.getCommonStyle());
        continue;
      }

      result.add(sequenceComponent);
    }
  }

  public List<Object> build() {
    popAllSequencesAndAddToResult(null);

    if (result.isEmpty())
      result.add(componentConstructor.createTextNode(""));

    return result;
  }
}
