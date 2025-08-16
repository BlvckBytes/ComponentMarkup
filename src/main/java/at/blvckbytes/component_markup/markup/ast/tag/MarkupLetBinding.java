/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.InputView;

public class MarkupLetBinding extends LetBinding {

  public final MarkupNode markup;
  public final boolean capture;

  public MarkupLetBinding(
    MarkupNode markup,
    InputView name,
    boolean capture
  ) {
    super(name);

    this.markup = markup;
    this.capture = capture;
  }
}
