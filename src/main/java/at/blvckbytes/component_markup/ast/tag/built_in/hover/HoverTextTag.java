package at.blvckbytes.component_markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.hover.TextHoverNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class HoverTextTag extends HoverTag {

  private final AttributeDefinition[] attributes;

  public HoverTextTag() {
    this.attributes = new AttributeDefinition[] {
      new AttributeDefinition("value", AttributeType.SUBTREE, false, true)
    };
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("hover-text");
  }

  @Override
  public TagClosing getClosing() {
    return TagClosing.OPEN_CLOSE;
  }

  @Override
  public TagPriority getPriority() {
    return TagPriority.NORMAL;
  }

  @Override
  public AttributeDefinition[] getAttributes() {
    return this.attributes;
  }

  @Override
  public AstNode construct(
    String tagName,
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
