package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.tag.AttributeDefinition;
import at.blvckbytes.component_markup.ast.tag.TagDefinition;

public abstract class HoverTag extends TagDefinition {

  protected HoverTag(AttributeDefinition[] attributes) {
    super(attributes);
  }
}
