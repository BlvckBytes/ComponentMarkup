package at.blvckbytes.component_markup.ast.node.control;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.xml.CursorPosition;

public class BreakNode extends AstNode {

  public BreakNode(CursorPosition position) {
    super(position, null, null);
  }
}
