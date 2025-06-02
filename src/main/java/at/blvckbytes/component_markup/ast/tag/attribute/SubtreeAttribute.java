package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.ast.node.AstNode;

public class SubtreeAttribute extends Attribute {

  public final AstNode value;

  protected SubtreeAttribute(String name, AstNode value) {
    super(name, value.position);

    this.value = value;
  }
}
