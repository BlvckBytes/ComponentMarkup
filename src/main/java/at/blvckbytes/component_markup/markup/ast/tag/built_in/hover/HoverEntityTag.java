package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.EntityHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverEntityTag extends HoverTag {

  private static final MandatoryExpressionAttributeDefinition ATTR_TYPE = new MandatoryExpressionAttributeDefinition("type");
  private static final MandatoryExpressionAttributeDefinition ATTR_ID = new MandatoryExpressionAttributeDefinition("id");
  private static final MarkupAttributeDefinition ATTR_NAME = new MarkupAttributeDefinition("name");

  public HoverEntityTag() {
    super(
      "hover-entity",
      ATTR_TYPE,
      ATTR_ID,
      ATTR_NAME
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
      ATTR_TYPE.single(attributes),
      ATTR_ID.single(attributes),
      ATTR_NAME.singleOrNull(attributes),
      position, children, letBindings
    );
  }
}
