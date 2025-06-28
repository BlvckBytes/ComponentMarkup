package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

import java.util.List;

public class ContainerNode extends StyledNode {

  public ContainerNode(
    CursorPosition position,
    List<MarkupNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);
  }
}
