/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

public class DotDouble {

  public final double value;

  private DotDouble(double value) {
    this.value = value;
  }

  public static DotDouble of(double value) {
    return new DotDouble(value);
  }
}
