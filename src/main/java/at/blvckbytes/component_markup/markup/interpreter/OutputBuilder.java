package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.BreakNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
    sequencesStack.peek().addSequence(sequence);
  }

  public @Nullable Object onTerminal(TerminalNode node, DelayedCreationHandler creationHandler) {
    return sequencesStack.peek().onTerminal(node, creationHandler);
  }

  private void popAllSequencesAndAddToResult(@Nullable List<MarkupNode> nonTerminalCollector) {
    while (!sequencesStack.isEmpty()) {
      ComponentSequence sequence = sequencesStack.pop();

      if (sequence.nonTerminal != null && nonTerminalCollector != null)
        nonTerminalCollector.add(sequence.nonTerminal);

      if (!sequencesStack.isEmpty()) {
        sequencesStack.peek().addSequence(sequence);
        continue;
      }

      Object combineResult = sequence.combine(componentConstructor);

      if (combineResult != null)
        result.add(combineResult);
    }
  }

  public List<Object> build() {
    popAllSequencesAndAddToResult(null);

    if (result.isEmpty())
      result.add(componentConstructor.createTextNode(""));

    return result;
  }
}
