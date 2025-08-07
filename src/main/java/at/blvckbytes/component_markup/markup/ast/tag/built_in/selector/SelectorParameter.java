/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.selector;

import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;
import at.blvckbytes.component_markup.platform.selector.TargetSelector;

public class SelectorParameter implements RendererParameter {

  public final TargetSelector selector;

  public SelectorParameter(TargetSelector selector) {
    this.selector = selector;
  }

  @Override
  public String asPlainText() {
    return toString();
  }

  @Override
  public String toString() {
    return "<SelectorParameter>";
  }
}
