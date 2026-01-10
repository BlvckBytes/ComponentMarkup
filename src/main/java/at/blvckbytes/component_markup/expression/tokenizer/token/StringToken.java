/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.JsonifyGetter;
import at.blvckbytes.component_markup.util.JsonifyIgnore;
import org.jetbrains.annotations.Nullable;

public class StringToken extends TerminalToken {

  // When instantiating views for test-cases, they are immediate, meaning that they do not
  // have the absolute offset-indices; we compare the value using #getPlainValue instead.
  @JsonifyIgnore
  public final InputView value;

  public StringToken(InputView raw, InputView value) {
    super(raw);

    this.value = value;
  }

  @Override
  @JsonifyGetter
  public @Nullable Object getPlainValue() {
    return value.buildString();
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__STRING;
  }
}