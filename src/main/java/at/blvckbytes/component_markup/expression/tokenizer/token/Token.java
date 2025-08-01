/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;

public abstract class Token {

  public final StringView raw;

  protected Token(StringView raw) {
    this.raw = raw;
  }

  public abstract TokenType getType();
}
