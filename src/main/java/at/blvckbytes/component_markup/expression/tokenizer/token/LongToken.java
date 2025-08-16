/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class LongToken extends TerminalToken {

  public final long value;

  public LongToken(InputView raw, long value) {
    super(raw);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__NUMBER;
  }
}
