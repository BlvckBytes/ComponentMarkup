/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.platform.ComponentConstructor;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Interpreter {

  TemporaryMemberEnvironment getEnvironment();

  @NotNull String evaluateAsString(@Nullable ExpressionNode expression);

  @Nullable String evaluateAsStringOrNull(@Nullable ExpressionNode expression);

  long evaluateAsLong(@Nullable ExpressionNode expression);

  @Nullable Long evaluateAsLongOrNull(@Nullable ExpressionNode expression);

  double evaluateAsDouble(@Nullable ExpressionNode expression);

  @Nullable Double evaluateAsDoubleOrNull(@Nullable ExpressionNode expression);

  @NotNull Number evaluateAsLongOrDouble(@Nullable ExpressionNode expressionNode);

  @Nullable Number evaluateAsLongOrDoubleOrNull(@Nullable ExpressionNode expressionNode);

  boolean evaluateAsBoolean(@Nullable ExpressionNode expression);

  TriState evaluateAsTriState(@Nullable ExpressionNode expression);

  @Nullable Object evaluateAsPlainObject(@Nullable ExpressionNode expression);

  void interpret(MarkupNode node);

  void interpret(MarkupNode node, @Nullable Runnable afterScopeBegin);

  OutputBuilder getCurrentBuilder();

  ComponentConstructor getComponentConstructor();

  int getCurrentSubtreeDepth();

}
