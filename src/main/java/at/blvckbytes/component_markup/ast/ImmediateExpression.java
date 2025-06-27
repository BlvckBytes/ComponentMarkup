package at.blvckbytes.component_markup.ast;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;

public class ImmediateExpression {

  private static final TerminalNode TRUE_EXPRESSION = new TerminalNode(new BooleanToken(0, true));
  private static final TerminalNode FALSE_EXPRESSION = new TerminalNode(new BooleanToken(0, false));
  private static final TerminalNode NULL_EXPRESSION = new TerminalNode(new NullToken(0));

  // TODO: I kind of don't like this class... Also, the lengths should not be 0 - they *are* known.

  public static ExpressionNode of(String value) {
    return new TerminalNode(new StringToken(0, value));
  }

  public static ExpressionNode of(long value) {
    return new TerminalNode(new LongToken(0, 0, value));
  }

  public static ExpressionNode of(double value) {
    return new TerminalNode(new DoubleToken(0, 0, value));
  }

  public static ExpressionNode of(boolean value) {
    return value ? TRUE_EXPRESSION : FALSE_EXPRESSION;
  }

  public static ExpressionNode ofNull() {
    return NULL_EXPRESSION;
  }
}
