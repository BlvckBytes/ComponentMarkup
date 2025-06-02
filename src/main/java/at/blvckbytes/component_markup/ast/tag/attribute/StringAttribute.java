package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.xml.CursorPosition;

public abstract class StringAttribute extends Attribute<String> {

  protected StringAttribute(String name, CursorPosition position) {
    super(name, position);
  }
}
