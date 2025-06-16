package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContainerTag extends TagDefinition {

  public static final String TAG_NAME = "container";
  public static final ContainerTag INSTANCE = new ContainerTag();

  private ContainerTag() {
    super(NO_ATTRIBUTES, new String[] { TAG_NAME }, TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(TAG_NAME);
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
    return new ContainerNode(position, children, letBindings);
  }
}
