package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.hover.TextHoverNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class HoverTextTag extends HoverTag {

  public HoverTextTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("value", AttributeType.SUBTREE, false, true)
      },
      "hover-text"
    );
  }

  @Override
  public AstNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new TextHoverNode(
      findSubtreeAttribute("value", attributes),
      position, children, letBindings
    );
  }
}
