/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class FunctionDrivenNode extends ExpressionNode {

  private final InputView positionProvider;

  public final BiFunction<InterpretationEnvironment, InterpreterLogger, @Nullable Object> function;

  public FunctionDrivenNode(
    InputView positionProvider,
    BiFunction<InterpretationEnvironment, InterpreterLogger, @Nullable Object> function
  ) {
    this.positionProvider = positionProvider;
    this.function = function;
  }

  @Override
  public InputView getFirstMemberPositionProvider() {
    return positionProvider;
  }

  @Override
  public InputView getLastMemberPositionProvider() {
    return positionProvider;
  }

  @Override
  public String toExpression() {
    return "<" + getClass().getName() + ">";
  }
}
