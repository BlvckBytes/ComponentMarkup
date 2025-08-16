/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;

public abstract class Token {

  public final InputView raw;

  protected Token(InputView raw) {
    this.raw = raw;
  }

  public abstract TokenType getType();
}
