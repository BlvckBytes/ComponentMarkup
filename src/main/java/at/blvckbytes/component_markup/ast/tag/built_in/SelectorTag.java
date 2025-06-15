package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.SelectorNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class SelectorTag extends TagDefinition {

  private static final String TAG_NAME = "selector";

  public SelectorTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("selector", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("separator", AttributeType.SUBTREE, false, false)
      },
      new String[] { TAG_NAME },
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(TAG_NAME);
  }

  @Override
  public AstNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new SelectorNode(
      findExpressionAttribute("selector", attributes),
      tryFindSubtreeAttribute("separator", attributes),
      position, letBindings
    );
  }
}
