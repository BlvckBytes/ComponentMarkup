/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class WhenMatchingNode extends MarkupNode {

  public final ExpressionNode input;
  public final WhenMatchingMap matchingMap;
  public final @Nullable MarkupNode other;

  public WhenMatchingNode(
    InputView positionProvider,
    ExpressionNode input,
    WhenMatchingMap matchingMap,
    @Nullable MarkupNode other
  ) {
    super(positionProvider, null, null);

    this.input = input;
    this.matchingMap = matchingMap;
    this.other = other;
  }
}
