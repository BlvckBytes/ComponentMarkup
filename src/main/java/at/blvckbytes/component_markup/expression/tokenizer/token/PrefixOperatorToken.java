package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;

public class PrefixOperatorToken extends ExpressionToken {

  public final PrefixOperator operator;

  public PrefixOperatorToken(int beginIndex, PrefixOperator operator) {
    super(beginIndex);

    this.operator = operator;
  }
}