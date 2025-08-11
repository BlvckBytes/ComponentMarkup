/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;

public class PrefixOperatorToken extends Token {

  public final PrefixOperator operator;

  public PrefixOperatorToken(StringView raw, PrefixOperator operator) {
    super(raw);

    this.operator = operator;
  }

  @Override
  public TokenType getType() {
    if (operator.isNamed)
      return TokenType.EXPRESSION__NAMED_PREFIX_OPERATOR;

    return TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY;
  }
}