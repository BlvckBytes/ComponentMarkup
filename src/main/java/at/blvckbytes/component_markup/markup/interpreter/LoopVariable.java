/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

public class LoopVariable implements InternalCopyable, DirectFieldAccess {

  public int index;
  public final int length;
  public boolean isFirst;
  public boolean isLast;
  public boolean isEven;
  public boolean isOdd;

  public LoopVariable(int length) {
    this.length = length;
  }

  public void setIndex(int index) {
    this.index = index;
    isEven = index % 2 == 0;
    isOdd = !isEven;
    isFirst = index == 0;
    isLast = index == length - 1;
  }

  @Override
  public Object copy() {
    LoopVariable copy = new LoopVariable(length);
    copy.index = index;
    copy.isFirst = isFirst;
    copy.isLast = isLast;
    copy.isEven = isEven;
    copy.isOdd = isOdd;
    return copy;
  }

  @Override
  public @Nullable Object accessField(String rawIdentifier) {
    switch (rawIdentifier) {
      case "index":
        return index;
      case "length":
        return length;
      case "is_first":
        return isFirst;
      case "is_last":
        return isLast;
      case "is_even":
        return isEven;
      case "is_odd":
        return isOdd;
      default:
        return DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
    }
  }
}
