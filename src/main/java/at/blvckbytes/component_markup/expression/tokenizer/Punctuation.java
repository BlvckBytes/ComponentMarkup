/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.InputView;

public enum Punctuation implements EnumToken {
  OPENING_PARENTHESIS('(', false),
  CLOSING_PARENTHESIS(')', true),
  OPENING_CURLY('{', false),
  CLOSING_CURLY('}', true),
  CLOSING_BRACKET(']', true),
  COMMA(',', false),
  COLON(':', false),
  ;

  public final char representation;
  public final boolean isClosing;

  Punctuation(char representation, boolean isClosing) {
    this.representation = representation;
    this.isClosing = isClosing;
  }

  @Override
  public String toString() {
    return String.valueOf(representation);
  }

  @Override
  public Token create(InputView raw) {
    return new PunctuationToken(raw, this);
  }

  @Override
  public int getLength() {
    return 1;
  }
}
