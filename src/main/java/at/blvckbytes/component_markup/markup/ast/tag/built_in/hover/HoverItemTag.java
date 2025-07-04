package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HoverItemTag extends HoverTag {

  private static final ExpressionAttributeDefinition ATTR_MATERIAL = new ExpressionAttributeDefinition("material");
  private static final ExpressionAttributeDefinition ATTR_AMOUNT = new ExpressionAttributeDefinition("amount");
  private static final MarkupAttributeDefinition ATTR_NAME = new MarkupAttributeDefinition("name");
  private static final MarkupAttributeDefinition ATTR_LORE = new MarkupAttributeDefinition("lore");

  public HoverItemTag() {
    super(
      "hover-item",
      ATTR_MATERIAL,
      ATTR_AMOUNT,
      ATTR_NAME,
      ATTR_LORE
    );
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new ItemHoverNode(
      ATTR_MATERIAL.singleOrNull(attributes),
      ATTR_AMOUNT.singleOrNull(attributes),
      ATTR_NAME.singleOrNull(attributes),
      ATTR_LORE.singleOrNull(attributes),
      position, children, letBindings
    );
  }
}
