package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.tag.AttributeDefinition;
import at.blvckbytes.component_markup.markup.ast.tag.TagClosing;
import at.blvckbytes.component_markup.markup.ast.tag.TagDefinition;
import at.blvckbytes.component_markup.markup.ast.tag.TagPriority;

public abstract class HoverTag extends TagDefinition {

  private final String tagName;

  protected HoverTag(AttributeDefinition[] attributes, String tagName) {
    super(attributes, new String[] { tagName }, TagClosing.OPEN_CLOSE, TagPriority.NORMAL);

    this.tagName = tagName;
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(this.tagName);
  }
}
