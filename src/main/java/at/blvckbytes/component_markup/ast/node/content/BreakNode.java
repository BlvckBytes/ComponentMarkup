package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class BreakNode extends ContentNode {

  public BreakNode(CursorPosition position, List<AstNode> children, List<LetBinding> letBindings) {
    super(position, children, letBindings);
  }
}
