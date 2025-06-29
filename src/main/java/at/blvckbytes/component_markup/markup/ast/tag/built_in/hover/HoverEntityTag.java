package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverEntityTag extends HoverTag {

  public HoverEntityTag() {
    super(
      new AttributeDefinition[] {
        new ExpressionAttributeDefinition("type", AttributeFlag.MANDATORY),
        new ExpressionAttributeDefinition("id", AttributeFlag.MANDATORY),
        new MarkupAttributeDefinition("name"),
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
      tryFindMarkupAttribute("name", attributes),
      position, children, letBindings
    );
  }
}
