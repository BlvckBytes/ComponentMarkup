package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class HoverEntityTag extends HoverTag {

  public HoverEntityTag() {
    super("hover-entity");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new EntityHoverNode(
      attributes.getMandatoryExpressionNode("type"),
      attributes.getMandatoryExpressionNode("id"),
      attributes.getOptionalMarkupNode("name"),
      tagName, children, letBindings
    );
  }
}
