package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.xml.CursorPosition;

public abstract class BooleanAttribute extends Attribute<Boolean> {

  protected BooleanAttribute(String name, CursorPosition position) {
    super(name, position);
  }
}
