package at.blvckbytes.component_markup.expression;

import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.util.StringView;

public class ImmediateExpression {

  public static TerminalNode ofNull(StringView sourceView) {
    return new TerminalNode(new NullToken(sourceView));
  }

  public static TerminalNode ofDouble(StringView sourceView, double value) {
    return new TerminalNode(new DoubleToken(sourceView, value));
  }

  public static TerminalNode ofBoolean(StringView sourceView, boolean value) {
    return new TerminalNode(new BooleanToken(sourceView, value));
  }

  public static TerminalNode ofLong(StringView sourceView, long value) {
    return new TerminalNode(new LongToken(sourceView, value));
  }

  public static TerminalNode ofString(StringView value) {
    return new TerminalNode(new StringToken(value, value.buildString()));
  }

  public static TerminalNode ofString(StringView sourceView, String value) {
    return new TerminalNode(new StringToken(sourceView, value));
  }
}
