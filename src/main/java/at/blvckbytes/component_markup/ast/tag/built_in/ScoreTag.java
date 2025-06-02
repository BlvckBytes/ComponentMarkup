package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ScoreNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public class ScoreTag extends TagDefinition {

  @Override
  public boolean matchName(String tagName) {
    return tagName.equalsIgnoreCase("score");
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
      new AttributeDefinition("name", AttributeType.STRING, false, true),
      new AttributeDefinition("objective", AttributeType.STRING, false, true),
      new AttributeDefinition("value", AttributeType.STRING, false, false)
    };
  }

  @Override
  public AstNode construct(
    String tagName,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new ScoreNode(
      getStringAttribute("name", attributes),
      getStringAttribute("objective", attributes),
      tryGetStringAttribute("value", attributes),
      children,
      letBindings
    );
  }
}
