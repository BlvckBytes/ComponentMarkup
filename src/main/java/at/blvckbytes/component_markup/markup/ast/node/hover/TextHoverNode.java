/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class TextHoverNode extends HoverNode {

  public final MarkupNode value;

  public TextHoverNode(
    MarkupNode value,
    InputView positionProvider,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, children, letBindings);

    this.value = value;
  }
}
