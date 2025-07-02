package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.TextHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverTextTag extends HoverTag {

  private static final MandatoryMarkupAttributeDefinition ATTR_VALUE = new MandatoryMarkupAttributeDefinition("value");

  public HoverTextTag() {
    super(
      "hover-text",
      ATTR_VALUE
    );
  }

  @Override
  public @NotNull MarkupNode createNode(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    return new TextHoverNode(
      ATTR_VALUE.single(attributes),
      position, children, letBindings
    );
  }
}
