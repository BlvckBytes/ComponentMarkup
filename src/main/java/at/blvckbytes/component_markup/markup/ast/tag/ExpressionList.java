package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionFlag;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.StringPosition;
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
      if (!attribute.flags.contains(ExpressionFlag.SPREAD_MODE)) {
        result.add(attribute.value);
        continue;
      }

      Object evaluatedValue = interpreter.evaluateAsPlainObject(attribute.value);

      StringPosition attributeBegin = attribute.value.getStartInclusive();
      StringView valueView = attributeBegin.rootView.buildSubViewAbsolute(
        attributeBegin.charIndex,
        attribute.value.getEndExclusive().charIndex
      );

      if (evaluatedValue instanceof Collection) {
        Collection<?> collection = (Collection<?>) evaluatedValue;

        for (Object item : collection)
          result.add(toTerminal(item, valueView));

        continue;
      }

      result.add(toTerminal(evaluatedValue, valueView));
    }

    return result;
  }

  private TerminalNode toTerminal(@Nullable Object value, StringView valueView) {
    if (value == null)
      return ImmediateExpression.ofNull(valueView);

    if (value instanceof Double || value instanceof Float)
      return ImmediateExpression.ofDouble(valueView, ((Number) value).doubleValue());

    if (value instanceof Number)
      return ImmediateExpression.ofLong(valueView, ((Number) value).longValue());

    if (value instanceof Boolean)
      return ImmediateExpression.ofBoolean(valueView, (boolean) value);

    return ImmediateExpression.ofString(valueView, String.valueOf(value));
  }
}
