/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.selector;

import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;
import org.jetbrains.annotations.Nullable;

public class SelectorParameter implements RendererParameter {

  public final String selector;
  public final @Nullable Object separator;

  public SelectorParameter(String selector, @Nullable Object separator) {
    this.selector = selector;
    this.separator = separator;
  }

  @Override
  public String asPlainText() {
    return toString();
  }

  @Override
  public String toString() {
    return "SelectorParameter{" +
      "selector='" + selector + '\'' +
      ", separator=" + separator +
      '}';
  }
}
