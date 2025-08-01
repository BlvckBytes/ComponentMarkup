/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.parser.AttributeFlag;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExpressionList {

  public static final ExpressionList EMPTY = new ExpressionList(0);

  private final List<ExpressionAttribute> attributes;

  public ExpressionList(int size) {
    this.attributes = new ArrayList<>(size);
  }

  public boolean isEmpty() {
    return attributes.isEmpty();
  }

  public void add(ExpressionAttribute attribute) {
    this.attributes.add(attribute);
  }

  public List<ExpressionNode> get(Interpreter interpreter) {
    List<ExpressionNode> result = new ArrayList<>(attributes.size());

    for (ExpressionAttribute attribute : attributes) {
      if (!attribute.attributeName.has(AttributeFlag.SPREAD_MODE)) {
        result.add(attribute.value);
        continue;
      }

      Object evaluatedValue = interpreter.evaluateAsPlainObject(attribute.value);

      if (evaluatedValue instanceof Collection) {
        Collection<?> collection = (Collection<?>) evaluatedValue;

        for (Object item : collection)
          result.add(toTerminal(item));

        continue;
      }

      result.add(toTerminal(evaluatedValue));
    }

    return result;
  }

  private TerminalNode toTerminal(@Nullable Object value) {
    if (value == null)
      return ImmediateExpression.ofNull();

    if (value instanceof Double || value instanceof Float)
      return ImmediateExpression.ofDouble(StringView.EMPTY, ((Number) value).doubleValue());

    if (value instanceof Number)
      return ImmediateExpression.ofLong(StringView.EMPTY, ((Number) value).longValue());

    if (value instanceof Boolean)
      return ImmediateExpression.ofBoolean(StringView.EMPTY, (boolean) value);

    return ImmediateExpression.ofString(StringView.EMPTY, String.valueOf(value));
  }
}
