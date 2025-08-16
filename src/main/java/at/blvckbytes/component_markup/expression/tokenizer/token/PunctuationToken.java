/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;

public class PunctuationToken extends Token {

  public final Punctuation punctuation;

  public PunctuationToken(InputView raw, Punctuation punctuation) {
    super(raw);

    this.punctuation = punctuation;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__PUNCTUATION__ANY;
  }
}
