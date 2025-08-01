/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.parser.ParserChildItem;

public class InterpolationNode extends StyledNode implements ParserChildItem {

  public final ExpressionNode contents;

  public InterpolationNode(ExpressionNode contents) {
    super(contents.getFirstMemberPositionProvider(), null, null);

    this.contents = contents;
  }
}
