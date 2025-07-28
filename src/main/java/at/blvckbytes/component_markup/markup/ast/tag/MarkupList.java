package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.ExpressionDrivenNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TextNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.parser.AttributeFlag;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MarkupList {

  public static final MarkupList EMPTY = new MarkupList(Collections.emptyList());

  private final List<Attribute> attributes;

  public MarkupList(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public boolean isEmpty() {
    return attributes.isEmpty();
  }

  public void add(Attribute attribute) {
    this.attributes.add(attribute);
  }

  public List<MarkupNode> get(Interpreter interpreter) {
    List<MarkupNode> result = new ArrayList<>(attributes.size());

    for (Attribute attribute : attributes) {
      if (attribute instanceof MarkupAttribute) {
        result.add(((MarkupAttribute) attribute).value);
        continue;
      }

      ExpressionAttribute expressionAttribute = (ExpressionAttribute) attribute;
      ExpressionNode expression = expressionAttribute.value;

      if (!expressionAttribute.attributeName.has(AttributeFlag.SPREAD_MODE)) {
        result.add(new ExpressionDrivenNode(expression));
        continue;
      }

      Object evaluatedValue = interpreter.evaluateAsPlainObject(expression);

      if (evaluatedValue instanceof Collection) {
        Collection<?> collection = (Collection<?>) evaluatedValue;

        for (Object item : collection)
          result.add(toNode(item));

        continue;
      }

      result.add(toNode(evaluatedValue));
    }

    return result;
  }

  private MarkupNode toNode(@Nullable Object value) {
    if (value instanceof MarkupNode)
      return (MarkupNode) value;

    return new TextNode(StringView.EMPTY, String.valueOf(value));
  }
}
