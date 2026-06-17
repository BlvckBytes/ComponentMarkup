/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.constructor.PlainTextComponentConstructor;
import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.ast.FunctionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.interpreter.MarkupInterpreter;
import at.blvckbytes.component_markup.markup.parser.AttributeName;

public class MarkupAttribute extends Attribute {

  public final MarkupNode value;

  public MarkupAttribute(AttributeName attributeName, MarkupNode value) {
    super(attributeName);

    this.value = value;
  }

  @Override
  public MarkupNode asMarkupNode() {
    return value;
  }

  public ExpressionAttribute asPlainTextExpressionAttribute() {
    return new ExpressionAttribute(attributeName, new FunctionDrivenNode(attributeName.fullName, (environment, interpreterLogger) -> (
      MarkupInterpreter.interpret(
        value, SlotType.CHAT, environment,
        PlainTextComponentConstructor.INSTANCE,
        interpreterLogger
      ).get(0)
    )));
  }
}
