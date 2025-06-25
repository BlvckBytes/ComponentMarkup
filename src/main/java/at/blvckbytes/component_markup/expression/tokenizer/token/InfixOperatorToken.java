package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;

public class InfixOperatorToken extends ExpressionToken {

  public final InfixOperator operator;

  public InfixOperatorToken(int beginIndex, InfixOperator operator) {
    super(beginIndex);

    this.operator = operator;
  }
}