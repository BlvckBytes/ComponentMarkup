/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.tag.TagClosing;
import at.blvckbytes.component_markup.markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.markup.ast.tag.TagPriority;
import at.blvckbytes.component_markup.util.StringView;

public abstract class HoverTag extends TagDefinition {

  private final String tagName;

  protected HoverTag(String tagName) {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);

    this.tagName = tagName;
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals(this.tagName, true);
  }
}
