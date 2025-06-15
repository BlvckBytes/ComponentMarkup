package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class ColorTag extends TagDefinition {

  private static final String TAG_NAME = "color";

  public ColorTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("value", AttributeType.EXPRESSION, false, true)
      },
      new String[] { TAG_NAME },
      TagClosing.OPEN_CLOSE,
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
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    wrapper.style.color = findExpressionAttribute("value", attributes);
    return wrapper;
  }
}
