package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.tooltip.EntityTooltipNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class EntityTooltipTag extends HoverTag {

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
      new AttributeDefinition("type", AttributeType.EXPRESSION, false, true),
      new AttributeDefinition("id", AttributeType.EXPRESSION, false, true),
      new AttributeDefinition("name", AttributeType.SUBTREE, false, false),
    };
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new EntityTooltipNode(
      findExpressionAttribute("type", attributes),
      findExpressionAttribute("id", attributes),
      tryFindSubtreeAttribute("name", attributes),
      position, children, letBindings
    );
  }
}
