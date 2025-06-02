package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.xml.CursorPosition;

public abstract class LongAttribute extends Attribute<Long> {

  protected LongAttribute(String name, CursorPosition position) {
    super(name, position);
  }
}
