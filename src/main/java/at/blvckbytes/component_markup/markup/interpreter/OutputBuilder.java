/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OutputBuilder {

  private final ComponentConstructor componentConstructor;
  private final @Nullable String breakString;

  private final List<Object> result;
  private @Nullable AddressTree deferredAddresses;

  private final Stack<ComponentSequence> sequencesStack;

  public OutputBuilder(
    @Nullable Object recipient,
    ComponentConstructor componentConstructor,
    Interpreter interpreter,
    SlotContext slotContext,
    SlotContext resetContext
  ) {
    this.componentConstructor = componentConstructor;
    this.breakString = slotContext.breakChar == 0 ? null : String.valueOf(slotContext.breakChar);
    this.sequencesStack = new Stack<>();
    this.sequencesStack.push(ComponentSequence.initial(recipient, slotContext, resetContext, componentConstructor, interpreter));
    this.result = new ArrayList<>();
  }

  public void onBreak() {
    if (breakString != null) {
      onText(new TextNode(StringView.EMPTY, breakString), null, false);
      return;
    }

    combineAllSequencesAndResult();
  }

  public void onNonTerminalBegin(MarkupNode nonTerminal) {
    sequencesStack.push(sequencesStack.peek().makeChildSequence(nonTerminal));
  }

  @SuppressWarnings("UnusedReturnValue")
  public @Nullable Object onNonTerminalEnd() {
    if (sequencesStack.isEmpty()) {
      LoggerProvider.log(Level.WARNING, "Encountered unbalanced non-terminal-stack");
      return null;
    }

    ComponentSequence sequence = sequencesStack.pop();
    return sequencesStack.peek().addSequence(sequence);
  }

  public void onText(TextNode node, @Nullable Consumer<Object> creationHandler, boolean doNotBuffer) {
    sequencesStack.peek().onText(node, creationHandler, doNotBuffer);
  }

  public void onUnit(UnitNode node, @Nullable Consumer<Object> creationHandler) {
    sequencesStack.peek().onUnit(node, creationHandler);
  }

  public void emitComponent(Object component) {
    sequencesStack.peek().emitComponent(component);
  }

  private void combineAllSequencesAndResult() {
    for (int index = sequencesStack.size() - 1; index >= 0; --index) {
      ComponentSequence sequence = sequencesStack.get(index);

      if (index == 0) {
        CombinationResult combinationResult = sequence.combineOrBubbleUpAndClearMembers(null);

        if (combinationResult != CombinationResult.NO_OP_SENTINEL) {

          // Apply the highest-up style manually now, without any further simplifying calculations
          if (combinationResult.styleToApply != null)
            combinationResult.styleToApply.applyStyles(combinationResult.component, componentConstructor);

          if (combinationResult.deferredAddresses != null) {
            if (deferredAddresses == null)
              deferredAddresses = new AddressTree();

            int deferredIndex = result.size();

            deferredAddresses.put(deferredIndex, combinationResult.deferredAddresses);
          }

          result.add(combinationResult.component);
        }

        break;
      }

      sequencesStack.get(index - 1).addSequence(sequence);
    }
  }

  public ComponentOutput build() {
    combineAllSequencesAndResult();

    if (result.isEmpty())
      result.add(componentConstructor.createTextComponent(""));

    sequencesStack.clear();

    return new ComponentOutput(result, deferredAddresses, componentConstructor);
  }
}
