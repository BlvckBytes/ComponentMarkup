package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class MarkupAttribute extends Attribute {

  public final MarkupNode value;

  public MarkupAttribute(CursorPosition position, String name, MarkupNode value) {
    super(position, name);

    this.value = value;
  }
}
