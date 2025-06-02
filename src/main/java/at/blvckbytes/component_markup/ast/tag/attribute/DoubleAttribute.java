package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.xml.CursorPosition;

public abstract class DoubleAttribute extends Attribute<Double> {

  protected DoubleAttribute(String name, CursorPosition position) {
    super(name, position);
  }
}
