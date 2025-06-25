package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.Operator;

public class OperatorToken extends ExpressionToken {

  public final Operator operator;

  public OperatorToken(int beginIndex, Operator operator) {
    super(beginIndex);

    this.operator = operator;
  }
}
