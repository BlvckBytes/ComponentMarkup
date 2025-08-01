/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.parser.AttributeName;

public class ExpressionAttribute extends Attribute {

  public final ExpressionNode value;

  public ExpressionAttribute(AttributeName attributeName, ExpressionNode value) {
    super(attributeName);

    this.value = value;
  }
}
