/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class NullToken extends TerminalToken {

  public NullToken(InputView raw) {
    super(raw);
  }

  @Override
  public @Nullable Object getPlainValue() {
    return null;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__LITERAL;
  }
}
