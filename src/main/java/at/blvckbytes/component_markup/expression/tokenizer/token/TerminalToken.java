/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public abstract class TerminalToken extends Token {

  protected TerminalToken(InputView raw) {
    super(raw);
  }

  public abstract @Nullable Object getPlainValue();
}
