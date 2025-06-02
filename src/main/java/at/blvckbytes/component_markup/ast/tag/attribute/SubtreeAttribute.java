package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.ast.node.AstNode;

public class SubtreeAttribute extends Attribute<AstNode> {

  private final AstNode value;

  protected SubtreeAttribute(String name, AstNode value) {
    super(name);

    this.value = value;
  }

  @Override
  public AstNode getValue() {
    return value;
  }
}
