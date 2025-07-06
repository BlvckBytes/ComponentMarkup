package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public abstract class Attribute {

  public final CursorPosition position;
  public final String name;

  public boolean hasBeenUsed;

  protected Attribute(CursorPosition position, String name) {
    this.position = position;
    this.name = name;
  }
}
