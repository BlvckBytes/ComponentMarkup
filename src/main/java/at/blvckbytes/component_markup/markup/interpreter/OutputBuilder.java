/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.*;
import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import at.blvckbytes.component_markup.constructor.SlotContext;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.logging.GlobalLogger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OutputBuilder<B, C> {

  private final MarkupInterpreter<B, C> interpreter;
  private final ComponentConstructor<B, C> componentConstructor;
  private final @Nullable String breakString;

  private final List<ExtendedBuilder<B>> result;

  private final Stack<ComponentSequence<B, C>> sequencesStack;

  public OutputBuilder(
    MarkupInterpreter<B, C> interpreter,
    SlotContext slotContext,
    SlotContext resetContext
  ) {
    this.interpreter = interpreter;
    this.componentConstructor = interpreter.getComponentConstructor();
    this.breakString = slotContext.breakChar == 0 ? null : String.valueOf(slotContext.breakChar);
    this.sequencesStack = new Stack<>();
    this.sequencesStack.push(ComponentSequence.initial(slotContext, resetContext, interpreter));
    this.result = new ArrayList<>();
  }

  public void onBreak() {
    if (breakString != null) {
      onText(new TextNode(InputView.EMPTY, breakString), null, false);
      return;
    }

    combineAllSequencesAndAddResult();
  }

  public void onNonTerminalBegin(MarkupNode nonTerminal) {
    sequencesStack.push(sequencesStack.peek().makeChildSequence(nonTerminal));
  }

  @SuppressWarnings("UnusedReturnValue")
  public @Nullable B onNonTerminalEnd() {
    if (sequencesStack.isEmpty()) {
      GlobalLogger.log(Level.WARNING, "Encountered unbalanced non-terminal-stack");
      return null;
    }

    ComponentSequence<B, C> sequence = sequencesStack.pop();
    return sequencesStack.peek().addSequence(sequence);
  }

  public void onText(TextNode node, @Nullable Consumer<B> creationHandler, boolean doNotBuffer) {
    sequencesStack.peek().onText(node, creationHandler, doNotBuffer);
  }

  public void onUnit(UnitNode node, @Nullable Consumer<B> creationHandler) {
    sequencesStack.peek().onUnit(node, creationHandler);
  }

  public void onComponent(C component, StyledNode containingNode) {
    sequencesStack.peek().onComponent(component, containingNode);
  }

  private void combineAllSequencesAndAddResult() {
    for (int index = sequencesStack.size() - 1; index >= 0; --index) {
      ComponentSequence<B, C> sequence = sequencesStack.get(index);

      if (index == 0) {
        ExtendedBuilder<B> combinationResult = sequence.combineOrBubbleUpAndClearMembers(null);

        if (combinationResult != null)
          result.add(combinationResult);

        break;
      }

      sequencesStack.get(index - 1).addSequence(sequence);
    }
  }

  public List<C> build() {
    combineAllSequencesAndAddResult();

    sequencesStack.clear();

    if (result.isEmpty()) {
      result.add(new ExtendedBuilder<>(componentConstructor.createTextComponent("")));
    }

    List<C> finalizedResult = new ArrayList<>();

    for (ExtendedBuilder<B> extendedBuilder : result)
      finalizedResult.add(extendedBuilder.toFinalizedComponent(componentConstructor, interpreter.getLogger()));

    return finalizedResult;
  }
}
