package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.AttributeDefinition;
import at.blvckbytes.component_markup.markup.ast.tag.AttributeType;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverItemTag extends HoverTag {

  public HoverItemTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("material", AttributeType.EXPRESSION, false, false),
        new AttributeDefinition("amount", AttributeType.EXPRESSION, false, false),
        new AttributeDefinition("name", AttributeType.SUBTREE, false, false),
        new AttributeDefinition("lore", AttributeType.SUBTREE, false, false)
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
      tryFindSubtreeAttribute("name", attributes),
      tryFindSubtreeAttribute("lore", attributes),
      position, children, letBindings
    );
  }
}
