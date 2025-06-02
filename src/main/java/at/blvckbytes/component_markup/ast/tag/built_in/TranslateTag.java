package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public class TranslateTag extends TagDefinition {

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("translate");
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
    return new AttributeDefinition[] {
      new AttributeDefinition("key", AttributeType.STRING, false, true),
      new AttributeDefinition("with", AttributeType.SUBTREE, true, false),
      new AttributeDefinition("fallback", AttributeType.SUBTREE, false, false)
    };
  }

  @Override
  public AstNode construct(
    String tagName,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new TranslateNode(
      getStringAttribute("key", attributes),
      getSubtreeAttributes("with", attributes),
      tryGetSubtreeAttribute("fallback", attributes),
      children, letBindings
    );
  }
}
