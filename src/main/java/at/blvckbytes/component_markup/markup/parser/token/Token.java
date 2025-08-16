/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.InputView;

public class Token {

  public final TokenType type;
  public final InputView value;

  public Token(TokenType type, InputView value) {
    this.type = type;
    this.value = value;
  }
}
