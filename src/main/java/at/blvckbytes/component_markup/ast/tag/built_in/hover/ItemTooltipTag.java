package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.tooltip.ItemTooltipNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class ItemTooltipTag extends HoverTag {

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("item-tooltip");
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
      new AttributeDefinition("material", AttributeType.EXPRESSION, false, false),
      new AttributeDefinition("amount", AttributeType.EXPRESSION, false, false),
      new AttributeDefinition("name", AttributeType.SUBTREE, false, false),
      new AttributeDefinition("lore", AttributeType.SUBTREE, false, false)
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
    return new ItemTooltipNode(
      findExpressionAttribute("material", attributes),
      tryFindExpressionAttribute("amount", attributes),
      tryFindSubtreeAttribute("name", attributes),
      tryFindSubtreeAttribute("lore", attributes),
      position, children, letBindings
    );
  }
}
