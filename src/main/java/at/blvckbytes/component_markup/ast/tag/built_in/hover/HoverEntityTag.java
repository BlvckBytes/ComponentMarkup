package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverEntityTag extends HoverTag {

  public HoverEntityTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("type", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("id", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("name", AttributeType.SUBTREE, false, false),
      },
      "hover-entity"
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
    return new EntityHoverNode(
      findExpressionAttribute("type", attributes),
      findExpressionAttribute("id", attributes),
      tryFindSubtreeAttribute("name", attributes),
      position, children, letBindings
    );
  }
}
