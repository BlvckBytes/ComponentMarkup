/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Interpreter {

  TemporaryMemberEnvironment getEnvironment();

  @NotNull String evaluateAsString(@Nullable ExpressionNode expression);

  @Nullable String evaluateAsStringOrNull(@Nullable ExpressionNode expression);

  long evaluateAsLong(@Nullable ExpressionNode expression);

  @Nullable Long evaluateAsLongOrNull(@Nullable ExpressionNode expression);

  double evaluateAsDouble(@Nullable ExpressionNode expression);

  @Nullable Double evaluateAsDoubleOrNull(@Nullable ExpressionNode expression);

  boolean evaluateAsBoolean(@Nullable ExpressionNode expression);

  TriState evaluateAsTriState(@Nullable ExpressionNode expression);

  @Nullable Object evaluateAsPlainObject(@Nullable ExpressionNode expression);

  ComponentOutput interpretSubtree(MarkupNode node, SlotContext slotContext);

  OutputBuilder getCurrentBuilder();

  ComponentConstructor getComponentConstructor();

  boolean isInSubtree();

}
