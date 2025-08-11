/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.OperatorFlag;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;

public class InfixOperatorToken extends Token {

  public final InfixOperator operator;

  public InfixOperatorToken(StringView raw, InfixOperator operator) {
    super(raw);

    this.operator = operator;
  }

  @Override
  public TokenType getType() {
    if (operator.flags.contains(OperatorFlag.NAMED))
      return TokenType.EXPRESSION__NAMED_INFIX_OPERATOR;

    return TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY;
  }
}