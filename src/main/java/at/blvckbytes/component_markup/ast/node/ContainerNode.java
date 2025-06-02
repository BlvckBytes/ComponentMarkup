package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class ContainerNode extends ContentNode {

  public ContainerNode(CursorPosition position, List<AstNode> children, List<LetBinding> letBindings) {
    super(position, children, letBindings);
  }
}
