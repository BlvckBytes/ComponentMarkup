/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;

import java.util.Collections;
import java.util.LinkedHashSet;

public class CaptureNode extends MarkupNode {

  public CaptureNode(MarkupNode node, LinkedHashSet<LetBinding> bindings) {
    super(node.positionProvider, Collections.singletonList(node), bindings);
  }
}
