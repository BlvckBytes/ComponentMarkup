/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.tag.*;

public abstract class ColorizeTag extends TagDefinition {

  protected ColorizeTag(TagPriority tagPriority) {
    super(TagClosing.OPEN_CLOSE, tagPriority);
  }

  protected ColorizeAttributes getBaseAttributes(AttributeMap attributes) {
    return new ColorizeAttributes(
      attributes.getOptionalExpressionNode("phase"),
      attributes.getOptionalExpressionNode("deep"),
      attributes.getOptionalExpressionNode("override-colors"),
      attributes.getOptionalExpressionNode("skip-whitespace"),
      attributes.getOptionalExpressionNode("skip-non-text"),
      attributes.getOptionalExpressionNode("merge-inner")
    );
  }
}
