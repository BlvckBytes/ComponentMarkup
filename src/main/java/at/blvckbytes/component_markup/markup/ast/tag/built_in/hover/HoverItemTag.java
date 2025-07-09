package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HoverItemTag extends HoverTag {

  public HoverItemTag() {
    super("hover-item");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new ItemHoverNode(
      attributes.getOptionalExpressionNode("material"),
      attributes.getOptionalExpressionNode("amount"),
      attributes.getOptionalMarkupNode("name"),
      attributes.getOptionalMarkupNode("lore"),
      attributes.getOptionalExpressionNode("hide-properties"),
      position, children, letBindings
    );
  }
}
