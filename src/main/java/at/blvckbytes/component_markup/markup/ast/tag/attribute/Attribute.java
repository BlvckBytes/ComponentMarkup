package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.util.StringView;

public abstract class Attribute {

  public final StringView name;

  public boolean hasBeenUsed;

  protected Attribute(StringView name) {
    this.name = name;
  }
}
