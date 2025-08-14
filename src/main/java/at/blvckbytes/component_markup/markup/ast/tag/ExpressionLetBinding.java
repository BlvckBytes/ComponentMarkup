/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringView;

public class ExpressionLetBinding extends LetBinding {

  public final ExpressionNode expression;
  public final boolean capture;

  public ExpressionLetBinding(
    ExpressionNode expression,
    StringView name,
    boolean capture
  ) {
    super(name);

    this.expression = expression;
    this.capture = capture;
  }
}
