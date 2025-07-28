package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.parser.AttributeName;

public class MarkupAttribute extends Attribute {

  public final MarkupNode value;

  public MarkupAttribute(AttributeName attributeName, MarkupNode value) {
    super(attributeName);

    this.value = value;
  }
}
