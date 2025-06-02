package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.BreakNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public class BreakTag extends TagDefinition {

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("br");
  }

  @Override
  public TagClosing getClosing() {
    return TagClosing.SELF_CLOSE;
  }

  @Override
  public TagPriority getPriority() {
    return TagPriority.NORMAL;
  }

  @Override
  public AttributeDefinition[] getAttributes() {
    return TagDefinition.NO_ATTRIBUTES;
  }

  @Override
  public AstNode construct(
    String tagName,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new BreakNode(children, letBindings);
  }
}
