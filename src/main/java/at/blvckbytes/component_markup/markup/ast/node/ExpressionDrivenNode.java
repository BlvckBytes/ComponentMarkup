/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;

public class ExpressionDrivenNode extends MarkupNode {

  public final ExpressionNode expression;

  public ExpressionDrivenNode(ExpressionNode expression) {
    super(expression.getFirstMemberPositionProvider(), null, null);

    this.expression = expression;
  }
}
