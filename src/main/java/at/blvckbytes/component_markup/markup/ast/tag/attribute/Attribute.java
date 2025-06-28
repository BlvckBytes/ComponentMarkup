package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public abstract class Attribute {

  public final String name;
  public final CursorPosition position;

  protected Attribute(String name, CursorPosition position) {
    this.name = name;
    this.position = position;
  }
}
