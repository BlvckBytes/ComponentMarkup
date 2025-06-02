package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;

import java.util.List;

public class ContainerNode extends ContentNode {

  public ContainerNode(List<AstNode> children, List<LetBinding> letBindings) {
    super(children, letBindings);
  }
}
