package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.ItemHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class HoverItemTag extends HoverTag {

  public HoverItemTag() {
    super("hover-item");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new ItemHoverNode(
      attributes.getOptionalExpressionNode("material"),
      attributes.getOptionalExpressionNode("amount"),
      attributes.getOptionalMarkupNode("name"),
      attributes.getOptionalMarkupNode("lore"),
      attributes.getOptionalExpressionNode("hide-properties"),
      tagName, children, letBindings
    );
  }
}
