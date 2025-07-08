package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public abstract class LetBinding {

  public final String name;
  public final CursorPosition position;

  public LetBinding(
    String name,
    CursorPosition position
  ) {
    this.name = name;
    this.position = position;
  }
}
