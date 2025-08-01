/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

public class FloatRangeValue implements ArgumentValue {

  public final float startInclusive;
  public final float endInclusive;

  public FloatRangeValue(float startInclusive, float endInclusive) {
    this.startInclusive = startInclusive;
    this.endInclusive = endInclusive;
  }
}
