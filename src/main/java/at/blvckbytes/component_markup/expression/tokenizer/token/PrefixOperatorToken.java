package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;

public class PrefixOperatorToken extends Token {

  public final PrefixOperator operator;

  public PrefixOperatorToken(int beginIndex, PrefixOperator operator) {
    super(beginIndex, beginIndex + (operator.length - 1));

    this.operator = operator;
  }
}