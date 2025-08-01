/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

public class IntegerRangeValue implements ArgumentValue {

  public final int startInclusive;
  public final int endInclusive;

  public IntegerRangeValue(int startInclusive, int endInclusive) {
    this.startInclusive = startInclusive;
    this.endInclusive = endInclusive;
  }
}
