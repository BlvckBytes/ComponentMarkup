package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class InsertTag extends TagDefinition {

  private final AttributeDefinition[] attributes;

  public InsertTag() {
    this.attributes = new AttributeDefinition[] {
      new AttributeDefinition("value", AttributeType.EXPRESSION, false, true)
    };
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("insert");
  }

  @Override
  public TagClosing getClosing() {
    return TagClosing.OPEN_CLOSE;
  }

  @Override
  public TagPriority getPriority() {
    return TagPriority.NORMAL;
  }

  @Override
  public AttributeDefinition[] getAttributes() {
    return attributes;
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new InsertNode(
      findExpressionAttribute("value", attributes),
      position, children, letBindings
    );
  }
}
