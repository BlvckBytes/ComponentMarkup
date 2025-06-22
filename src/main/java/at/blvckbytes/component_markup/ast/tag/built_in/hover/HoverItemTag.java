package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  public @NotNull AstNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
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
