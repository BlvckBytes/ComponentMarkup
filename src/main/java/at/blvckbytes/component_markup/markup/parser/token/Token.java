/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.StringView;

public class Token {

  public final TokenType type;
  public final StringView value;

  public Token(TokenType type, StringView value) {
    this.type = type;
    this.value = value;
  }
}
