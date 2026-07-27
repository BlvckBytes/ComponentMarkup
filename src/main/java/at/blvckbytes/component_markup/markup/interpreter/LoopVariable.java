/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoopVariable implements InternalCopyable, DirectFieldAccess {

  public int index;
  public final int length;
  public boolean isFirst;
  public boolean isLast;
  public boolean isEven;
  public boolean isOdd;
  public @Nullable Object prior;
  public @Nullable Object next;

  public LoopVariable(int length) {
    this.length = length;
  }

  public void setIndex(int index, List<?> items) {
    this.index = index;
    isEven = index % 2 == 0;
    isOdd = !isEven;
    isFirst = index == 0;
    isLast = index == length - 1;
    prior = index == 0 ? null : items.get(index - 1);
    next = index + 1 < items.size() ? items.get(index + 1) : null;
  }

  @Override
  public Object copy() {
    LoopVariable copy = new LoopVariable(length);
    copy.index = index;
    copy.isFirst = isFirst;
    copy.isLast = isLast;
    copy.isEven = isEven;
    copy.isOdd = isOdd;
    copy.prior = prior;
    copy.next = next;
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
      case "prior":
        return prior;
      case "next":
        return next;
      default:
        return DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
    }
  }

  @Override
  public Set<String> getAvailableFields() {
    Set<String> fields = new HashSet<>();

    fields.add("index");
    fields.add("length");
    fields.add("is_first");
    fields.add("is_last");
    fields.add("is_even");
    fields.add("is_odd");
    fields.add("prior");
    fields.add("next");

    return fields;
  }
}
