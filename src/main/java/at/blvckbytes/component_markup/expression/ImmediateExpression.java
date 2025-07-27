package at.blvckbytes.component_markup.expression;

import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.util.StringView;

public class ImmediateExpression {

  public static TerminalNode ofNull() {
    return new TerminalNode(new NullToken(StringView.EMPTY));
  }

  public static TerminalNode ofDouble(double value) {
    return new TerminalNode(new DoubleToken(StringView.EMPTY, value));
  }

  public static TerminalNode ofBoolean(boolean value) {
    return new TerminalNode(new BooleanToken(StringView.EMPTY, value));
  }

  public static TerminalNode ofLong(long value) {
    return new TerminalNode(new LongToken(StringView.EMPTY, value));
  }

  public static TerminalNode ofString(StringView value) {
    return new TerminalNode(new StringToken(StringView.EMPTY, value.buildString()));
  }

  public static TerminalNode ofString(String value) {
    return new TerminalNode(new StringToken(StringView.EMPTY, value));
  }
}
