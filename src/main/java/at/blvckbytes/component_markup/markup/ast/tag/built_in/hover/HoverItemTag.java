package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverItemTag extends HoverTag {

  public HoverItemTag() {
    super(
      new AttributeDefinition[] {
        new ExpressionAttributeDefinition("material", false, false),
        new ExpressionAttributeDefinition("amount", false, false),
        new MarkupAttributeDefinition("name", false, false),
        new MarkupAttributeDefinition("lore", false, false)
      },
      "hover-item"
    );
  }

  @Override
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    return new ItemHoverNode(
      findExpressionAttribute("material", attributes),
      tryFindExpressionAttribute("amount", attributes),
      tryFindMarkupAttribute("name", attributes),
      tryFindMarkupAttribute("lore", attributes),
      position, children, letBindings
    );
  }
}
