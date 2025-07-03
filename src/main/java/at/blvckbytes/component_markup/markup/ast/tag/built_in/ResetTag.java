package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResetTag extends TagDefinition {

  protected ResetTag() {
    super(
      new String[]{ "r", "reset" },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("r") || tagNameLower.equals("reset");
  }

  @Override
  public @NotNull MarkupNode createNode(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    ContainerNode result = new ContainerNode(position, children, letBindings);
    result.doesResetStyle = true;
    return result;
  }
}
