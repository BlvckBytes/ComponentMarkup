package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.StringView;

public class BreakNode extends MarkupNode {

  public BreakNode(StringView positionProvider) {
    super(positionProvider, null, null);
  }
}
