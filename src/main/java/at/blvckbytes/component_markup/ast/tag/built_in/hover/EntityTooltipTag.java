package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public class EntityTooltipTag implements HoverTag {

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("entity-tooltip");
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
    return new AttributeDefinition[] {
      new AttributeDefinition("type", AttributeType.STRING, false, true),
      new AttributeDefinition("id", AttributeType.STRING, false, true),
      new AttributeDefinition("name", AttributeType.SUBTREE, false, false),
    };
  }

  @Override
  public AstNode construct(String tagName, List<Attribute> attributes, List<AstNode> members) {
    throw new UnsupportedOperationException();
  }
}
