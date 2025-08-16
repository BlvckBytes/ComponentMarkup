/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.InputView;

public class BreakNode extends MarkupNode {

  public BreakNode(InputView positionProvider) {
    super(positionProvider, null, null);
  }
}
