package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;

public class MarkupAttribute extends Attribute {

  public final MarkupNode value;

  public MarkupAttribute(String name, MarkupNode value) {
    super(name);

    this.value = value;
  }
}
