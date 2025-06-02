package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ItemTooltipNode;
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
      new AttributeDefinition("material", AttributeType.STRING, false, false),
      new AttributeDefinition("amount", AttributeType.LONG, false, false),
      new AttributeDefinition("name", AttributeType.SUBTREE, false, true),
      new AttributeDefinition("lore", AttributeType.SUBTREE, false, false)
    };
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    Long amount = tryGetLongAttribute("amount", attributes);

    return new ItemTooltipNode(
      getStringAttribute("material", attributes),
      amount == null ? 1 : amount,
      getSubtreeAttribute("name", attributes),
      tryGetSubtreeAttribute("lore", attributes),
      position,
      children,
      letBindings
    );
  }
}
