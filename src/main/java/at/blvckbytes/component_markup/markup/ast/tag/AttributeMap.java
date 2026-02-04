/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AttributeMap {

  @NotNull ExpressionNode getMandatoryExpressionNode(String name, String... aliases);

  @Nullable ExpressionNode getOptionalExpressionNode(String name, String... aliases);

  @Nullable ExpressionNode getOptionalBoundFlagExpressionNode();

  @NotNull ExpressionList getMandatoryExpressionList(String name, String... aliases);

  @NotNull ExpressionList getOptionalExpressionList(String name, String... aliases);

  @NotNull ExpressionList getOptionalBoundFlagExpressionList();

  @NotNull MarkupNode getMandatoryMarkupNode(String name, String... aliases);

  @Nullable MarkupNode getOptionalMarkupNode(String name, String... aliases);

  @Nullable MarkupNode getOptionalBoundFlagMarkupNode();

  @NotNull MarkupList getMandatoryMarkupList(String name, String... aliases);

  @NotNull MarkupList getOptionalMarkupList(String name, String... aliases);

  @NotNull MarkupList getOptionalBoundFlagMarkupList();

}
