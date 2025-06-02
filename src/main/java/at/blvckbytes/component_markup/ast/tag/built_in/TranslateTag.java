package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

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
      new AttributeDefinition("key", AttributeType.EXPRESSION, false, true),
      new AttributeDefinition("with", AttributeType.SUBTREE, true, false),
      new AttributeDefinition("fallback", AttributeType.SUBTREE, false, false)
    };
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new TranslateNode(
      findExpressionAttribute("key", attributes),
      findSubtreeAttributes("with", attributes),
      tryFindSubtreeAttribute("fallback", attributes),
      position, children, letBindings
    );
  }
}
