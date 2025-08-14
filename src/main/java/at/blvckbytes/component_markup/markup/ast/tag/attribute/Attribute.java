/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.parser.AttributeName;

public abstract class Attribute {

  public final AttributeName attributeName;

  public boolean hasBeenUsed;

  protected Attribute(AttributeName attributeName) {
    this.attributeName = attributeName;
  }

  public abstract MarkupNode asMarkupNode();
}
