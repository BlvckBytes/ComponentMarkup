/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

import at.blvckbytes.component_markup.util.InputView;

public class StringValue implements ArgumentValue {

  public final InputView raw;
  public final String value;
  public final boolean negated;

  public StringValue(InputView raw, String value, boolean negated) {
    this.raw = raw;
    this.value = value;
    this.negated = negated;
  }

  @Override
  public boolean isNegated() {
    return negated;
  }
}
