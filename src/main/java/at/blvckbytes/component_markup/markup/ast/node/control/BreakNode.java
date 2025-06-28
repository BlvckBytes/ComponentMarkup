package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class BreakNode extends MarkupNode {

  public BreakNode(CursorPosition position) {
    super(position, null, null);
  }
}
