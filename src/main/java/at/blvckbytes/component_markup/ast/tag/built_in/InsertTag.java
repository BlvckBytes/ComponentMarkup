package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class InsertTag extends TagDefinition {

  private static final String TAG_NAME = "insert";

  public InsertTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("value", AttributeType.EXPRESSION, false, true)
      },
      new String[] { TAG_NAME }
    );
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equals(TAG_NAME);
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
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new InsertNode(
      findExpressionAttribute("value", attributes),
      position, children, letBindings
    );
  }
}
