/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

import at.blvckbytes.component_markup.util.StringView;

public class NumericValue implements ArgumentValue {

  public final StringView raw;
  public final Number value;

  public final boolean isDouble;
  public final boolean isNegative;
  public final boolean isNegated;

  public NumericValue(
    StringView raw,
    Number value,
    boolean isDouble,
    boolean isNegative,
    boolean isNegated
  ) {
    this.raw = raw;
    this.value = value;
    this.isDouble = isDouble;
    this.isNegative = isNegative;
    this.isNegated = isNegated;
  }
}
