/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.test_utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListBuilder<T> {

  private final List<Object> result;
  public final Class<T> type;

  public ListBuilder(Class<T> type) {
    this.result = new ArrayList<>();
    this.type = type;
  }

  public ListBuilder<T> add(Object element) {
    if (element != null && !type.isAssignableFrom(element.getClass()))
      throw new IllegalStateException("Unsupported type " + element.getClass() + ", expected " + type);

    this.result.add(element);
    return this;
  }

  public ListBuilder<T> with(Consumer<ListBuilder<T>> handler) {
    handler.accept(this);
    return this;
  }

  @SuppressWarnings("unchecked")
  public List<T> getResult() {
    return (List<T>) this.result;
  }
}
