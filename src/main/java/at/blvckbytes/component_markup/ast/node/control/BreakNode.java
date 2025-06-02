package at.blvckbytes.component_markup.ast.node.control;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.xml.CursorPosition;

public class BreakNode extends AstNode {

  public BreakNode(CursorPosition position) {
    super(position, null, null);
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "BreakNode{\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
