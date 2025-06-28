package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.ast.node.MarkupNode;

public class SubtreeAttribute extends Attribute {

  public final MarkupNode value;

  public SubtreeAttribute(String name, MarkupNode value) {
    super(name, value.position);

    this.value = value;
  }
}
