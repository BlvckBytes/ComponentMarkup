package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class FontTag extends TagDefinition {

  private static final String TAG_NAME = "font";

  public FontTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("name", AttributeType.EXPRESSION, false, true)
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
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    wrapper.style.font = findExpressionAttribute("name", attributes);
    return wrapper;
  }
}
