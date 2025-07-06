package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionFlag;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
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
      int beginIndex = attribute.value.getBeginIndex();

      if (evaluatedValue instanceof Collection) {
        Collection<?> collection = (Collection<?>) evaluatedValue;

        for (Object item : collection)
          result.add(toTerminal(item, beginIndex));

        continue;
      }

      result.add(toTerminal(evaluatedValue, beginIndex));
    }

    return result;
  }

  private TerminalNode toTerminal(@Nullable Object value, int beginIndex) {
    if (value == null)
      return new TerminalNode(new NullToken(beginIndex, ""));

    if (value instanceof Double || value instanceof Float)
      return new TerminalNode(new DoubleToken(beginIndex, "", ((Number) value).doubleValue()));

    if (value instanceof Number)
      return new TerminalNode(new LongToken(beginIndex, "", ((Number) value).longValue()));

    if (value instanceof Boolean)
      return new TerminalNode(new BooleanToken(beginIndex, "", (boolean) value));

    return new TerminalNode(new StringToken(beginIndex, String.valueOf(value)));
  }
}
