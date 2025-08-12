/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Function;

public class DeepIterator<T> implements Iterator<T> {

  private final Stack<Iterator<?>> iteratorStack;
  private final Function<Object, T> mapper;

  public DeepIterator(Iterable<?> iterable, Function<Object, T> mapper) {
    this.mapper = mapper;
    this.iteratorStack = new Stack<>();
    this.iteratorStack.push(iterable.iterator());
  }

  @Override
  public boolean hasNext() {
    while (!iteratorStack.empty() && !iteratorStack.peek().hasNext())
      iteratorStack.pop();

    if (iteratorStack.empty())
      return false;

    return iteratorStack.peek().hasNext();
  }

  @Override
  public T next() {
    while (!iteratorStack.empty() && !iteratorStack.peek().hasNext())
      iteratorStack.pop();

    if (iteratorStack.empty())
      return null;

    Object value = iteratorStack.peek().next();

    if (!(value instanceof Iterable<?>))
      return mapper.apply(value);

    iteratorStack.push(((Iterable<?>) value).iterator());

    return next();
  }
}
