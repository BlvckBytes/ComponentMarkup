/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class EntityHoverNode extends HoverNode {

  public final ExpressionNode type;
  public final ExpressionNode id;
  public final @Nullable MarkupNode name;

  public EntityHoverNode(
    ExpressionNode type,
    ExpressionNode id,
    @Nullable MarkupNode name,
    StringView positionProvider,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, children, letBindings);

    this.type = type;
    this.id = id;
    this.name = name;
  }
}
