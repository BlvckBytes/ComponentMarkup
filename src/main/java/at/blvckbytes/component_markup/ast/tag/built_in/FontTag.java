package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FontTag extends TagDefinition {

  private static final String TAG_NAME = "font";

  public FontTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("name", AttributeType.EXPRESSION, false, true)
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
  public boolean modifyContainer(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    ContainerNode container
  ) {
    container.style.font = findExpressionAttribute("name", attributes);
    return true;
  }

  @Override
  public @Nullable AstNode construct(
    String tagNameLower,
    boolean didModifyContainer,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    if (didModifyContainer)
      return null;

    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    wrapper.style.font = findExpressionAttribute("name", attributes);
    return wrapper;
  }
}
