package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.StringPosition;

public class BreakNode extends MarkupNode {

  public BreakNode(StringPosition position) {
    super(position, null, null);
  }
}
