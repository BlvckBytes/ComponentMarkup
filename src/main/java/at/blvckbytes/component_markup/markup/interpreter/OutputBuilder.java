package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.markup.ast.node.hover.AchievementHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.TextHoverNode;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class OutputBuilder {

  private final ComponentConstructor componentConstructor;
  private final Interpreter interpreter;
  private final char breakChar;

  private final List<Object> result;
  private final Stack<ComponentSequence> sequencesStack;

  public OutputBuilder(
    ComponentConstructor componentConstructor,
    Interpreter interpreter,
    char breakChar
  ) {
    this.componentConstructor = componentConstructor;
    this.interpreter = interpreter;
    this.breakChar = breakChar;
    this.sequencesStack = new Stack<>();
    this.sequencesStack.push(new ComponentSequence(null, interpreter));
    this.result = new ArrayList<>();
  }

  public void onBreak() {
    if (breakChar != 0) {
      sequencesStack.peek().members.add(componentConstructor.createKeyNode(String.valueOf(breakChar)));
      return;
    }

    List<MarkupNode> poppedNonTerminals = new ArrayList<>();

    popAllSequencesAndAddToResult(poppedNonTerminals);

    sequencesStack.push(new ComponentSequence(null, interpreter));

    for (MarkupNode poppedNonTerminal : poppedNonTerminals)
      onNonTerminalBegin(poppedNonTerminal);
  }

  public void onNonTerminalBegin(MarkupNode node) {
    sequencesStack.push(new ComponentSequence(node, interpreter));
  }

  public void onNonTerminalEnd() {
    if (sequencesStack.isEmpty())
      throw new IllegalStateException("Encountered unbalanced non-terminal-stack");

    Object item = popAndCombineSequence(null);
    sequencesStack.peek().members.add(item);
  }

  private Object popAndCombineSequence(@Nullable List<MarkupNode> nonTerminalCollector) {
    ComponentSequence sequence = sequencesStack.pop();

    if (sequence.members.isEmpty())
      return componentConstructor.createTextNode("");

    Object sequenceComponent;

    if (sequence.members.size() == 1) {
      sequenceComponent = sequence.members.get(0);
    } else {
      sequenceComponent = componentConstructor.createTextNode("");
      componentConstructor.setChildren(sequenceComponent, sequence.members);
    }

    if (sequence.computedStyle != null)
      sequence.computedStyle.applyStyles(sequenceComponent, componentConstructor);

    if (sequence.nonTerminal == null)
      return sequenceComponent;

    if (nonTerminalCollector != null)
      nonTerminalCollector.add(sequence.nonTerminal);

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
          URI uri;

          try {
            uri = URI.create(interpreter.evaluateAsString(clickNode.value));
          } catch (Throwable e) {
            uri = URI.create("https://google.com");
            // TODO: Log about encountering malformed URI
          }

          componentConstructor.setClickOpenUrlAction(sequenceComponent, uri);
          break;
        }

        default:
          throw new IllegalStateException("Unknown click-action: " + clickNode.action);
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

      if (entityHoverNode.name != null)
        name = interpreter.interpret(entityHoverNode.name, ' ').get(0);

      UUID uuid;

      try {
        uuid = UUID.fromString(id);
      } catch (Throwable e) {
        uuid = UUID.randomUUID();
        // TODO: Log about encountering malformed URI
      }

      componentConstructor.setHoverEntityAction(sequenceComponent, type, uuid, name);
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

      if (itemHoverNode.name != null)
        name = interpreter.interpret(itemHoverNode.name, ' ').get(0);

      List<Object> lore = null;

      if (itemHoverNode.lore != null)
        lore = interpreter.interpret(itemHoverNode.lore, (char) 0);

      componentConstructor.setHoverItemAction(sequenceComponent, material, count, name, lore);
    }

    else if (sequence.nonTerminal instanceof TextHoverNode) {
      TextHoverNode textHoverNode = (TextHoverNode) sequence.nonTerminal;

      List<Object> text = interpreter.interpret(textHoverNode.value, (char) 0);

      if (text.size() == 1)
        componentConstructor.setHoverTextAction(sequenceComponent, text.get(0));
      else {
        Object name = text.remove(0);
        componentConstructor.setHoverItemAction(sequenceComponent, "dirt", null, name, text);
      }
    }

    return sequenceComponent;
  }

  public Object onTerminal(TerminalNode node) {
    Object result;

    if (node instanceof TextNode) {
      String text = interpreter.evaluateAsString(((TextNode) node).text);
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

      if (selectorNode.separator != null)
        separator = interpreter.interpret(selectorNode.separator, ' ').get(0);

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

      if (nbtNode.separator != null)
        separator = interpreter.interpret(nbtNode.separator, ' ').get(0);

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
          throw new IllegalStateException("Unknown nbt-source: " + nbtNode.source);
      }
    }

    else if (node instanceof TranslateNode) {
      TranslateNode translateNode = (TranslateNode) node;

      String key = interpreter.evaluateAsString(translateNode.key);

      List<Object> with = new ArrayList<>();

      for (MarkupNode withNode : translateNode.with)
        with.add(interpreter.interpret(withNode, ' ').get(0));

      String fallback = null;

      if (translateNode.fallback != null)
        fallback = interpreter.evaluateAsStringOrNull(translateNode.fallback);

      result = componentConstructor.createTranslateNode(key, with, fallback);
    }

    else
      throw new IllegalStateException("Unknown terminal-node: " + node.getClass());

    new ComputedStyle(node, interpreter).applyStyles(result, componentConstructor);

    sequencesStack.peek().members.add(result);

    return result;
  }

  private void popAllSequencesAndAddToResult(@Nullable List<MarkupNode> nonTerminalCollector) {
    while (!sequencesStack.isEmpty()) {
      Object sequenceComponent = popAndCombineSequence(nonTerminalCollector);

      if (!sequencesStack.isEmpty()) {
        sequencesStack.peek().members.add(sequenceComponent);
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
