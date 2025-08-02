/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

public class NumericRangeValue implements ArgumentValue {

  public final NumericValue startInclusive;
  public final NumericValue endInclusive;

  public NumericRangeValue(NumericValue startInclusive, NumericValue endInclusive) {
    this.startInclusive = startInclusive;
    this.endInclusive = endInclusive;
  }

  @Override
  public boolean isNegated() {
    return false;
  }
}
