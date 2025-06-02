package at.blvckbytes.component_markup.ast;

import me.blvckbytes.gpeee.parser.LiteralType;
import me.blvckbytes.gpeee.parser.expression.*;

public class ImmediateExpression {

  private static final LiteralExpression TRUE_EXPRESSION = new LiteralExpression(LiteralType.TRUE, null, null, null);
  private static final LiteralExpression FALSE_EXPRESSION = new LiteralExpression(LiteralType.FALSE, null, null, null);
  private static final LiteralExpression NULL_EXPRESSION = new LiteralExpression(LiteralType.NULL, null, null, null);

  public static AExpression of(String value) {
    return new StringExpression(value, null, null, null);
  }

  public static AExpression of(long value) {
    return new LongExpression(value, null, null, null);
  }

  public static AExpression of(double value) {
    return new DoubleExpression(value, null, null, null);
  }

  public static AExpression of(boolean value) {
    return value ? TRUE_EXPRESSION : FALSE_EXPRESSION;
  }

  public static AExpression ofNull() {
    return NULL_EXPRESSION;
  }
}
