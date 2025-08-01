/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class StringToken extends TerminalToken {

  public final String value;

  public StringToken(StringView raw, String value) {
    super(raw);

    this.value = value;
  }

  public StringToken(StringView raw, StringView value) {
    super(raw);

    this.value = value.buildString();
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__STRING;
  }
}
