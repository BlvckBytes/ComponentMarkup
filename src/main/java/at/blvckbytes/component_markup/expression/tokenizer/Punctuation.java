/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.StringView;

public enum Punctuation implements EnumToken {
  OPENING_PARENTHESIS('('),
  CLOSING_PARENTHESIS(')'),
  CLOSING_BRACKET(']'),
  COMMA(','),
  COLON(':'),
  ;

  public final char representation;

  Punctuation(char representation) {
    this.representation = representation;
  }

  @Override
  public String toString() {
    return String.valueOf(representation);
  }

  @Override
  public Token create(StringView raw) {
    return new PunctuationToken(raw, this);
  }

  @Override
  public int getLength() {
    return 1;
  }
}
