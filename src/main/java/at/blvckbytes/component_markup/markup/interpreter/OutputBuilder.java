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
import java.util.logging.Level;

public class OutputBuilder<B, C> {

  private final MarkupInterpreter<B, C> interpreter;
  private final SlotContext slotContext;
  private final ComponentConstructor<B, C> componentConstructor;
  private final @Nullable String breakString;

  private final List<ExtendedBuilder<B>> result;

  private final Stack<ComponentSequence<B, C>> sequencesStack;

  private int totalTextLength;
  private int totalUnitCount;

  private boolean hasTrailingComponentBreak;

  public OutputBuilder(
    MarkupInterpreter<B, C> interpreter,
    SlotContext slotContext,
    SlotContext resetContext
  ) {
    this.interpreter = interpreter;
    this.slotContext = slotContext;
    this.componentConstructor = interpreter.getComponentConstructor();
    this.breakString = slotContext.breakChar == 0 ? null : String.valueOf(slotContext.breakChar);
    this.sequencesStack = new Stack<>();
    this.sequencesStack.push(ComponentSequence.initial(slotContext, resetContext, interpreter));
    this.result = new ArrayList<>();
  }

  public int getTotalTextLength() {
    return totalTextLength;
  }

  public int getTotalUnitCount() {
    return totalUnitCount;
  }

  public boolean hasContent() {
    return totalTextLength > 0 || totalUnitCount > 0;
  }

  public SlotContext getSlotContext() {
    return slotContext;
  }

  public void onBreak() {
    if (breakString != null) {
      onText(new TextNode(InputView.EMPTY, breakString), null, false);
      return;
    }

    combineAllSequencesAndAddResult();
    hasTrailingComponentBreak = true;
  }

  public void onNonTerminalBegin(MarkupNode nonTerminal) {
    sequencesStack.push(sequencesStack.peek().makeChildSequence(nonTerminal));
  }

  public void onNonTerminalEnd() {
    if (sequencesStack.isEmpty()) {
      GlobalLogger.log(Level.WARNING, "Encountered unbalanced non-terminal-stack");
      return;
    }

    ComponentSequence<B, C> sequence = sequencesStack.pop();
    sequencesStack.peek().addSequence(sequence);
  }

  public void onText(TextNode node, @Nullable CreationHandler<B> creationHandler, boolean doNotBuffer) {
    totalTextLength += node.textValue.length();
    sequencesStack.peek().onText(node, creationHandler, doNotBuffer);
    hasTrailingComponentBreak = false;
  }

  public void onUnit(UnitNode node, @Nullable CreationHandler<B> creationHandler) {
    ++totalUnitCount;
    sequencesStack.peek().onUnit(node, creationHandler);
    hasTrailingComponentBreak = false;
  }

  public void onComponent(C component, StyledNode containingNode) {
    componentConstructor.forEachTextOf(component, text -> totalTextLength += text.length());
    componentConstructor.forEachNonTextUnitOf(component, unit -> ++totalUnitCount);
    sequencesStack.peek().onComponent(component, containingNode);
    hasTrailingComponentBreak = false;
  }

  public void appendBuilder(OutputBuilder<B, C> builder) {
    if (!builder.hasContent() && builder.hasTrailingComponentBreak) {
      onBreak();
      return;
    }

    this.totalUnitCount += builder.totalUnitCount;
    this.totalTextLength += builder.totalTextLength;

    if (builder.result.isEmpty() && builder.sequencesStack.size() == 1) {
      if (sequencesStack.peek().tryAddBufferedTextFromSequence(builder.sequencesStack.peek())) {
        builder.sequencesStack.clear();
        return;
      }
    }

    builder.combineAllSequencesAndAddResult();
    builder.sequencesStack.clear();

    for (int resultIndex = 0; resultIndex < builder.result.size(); ++resultIndex) {
      if (resultIndex > 0)
        onBreak();

      if (builder.hasTrailingComponentBreak && resultIndex == builder.result.size() - 1)
        continue;

      sequencesStack.peek().addMember(builder.result.get(resultIndex));
    }
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
