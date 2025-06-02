package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class InsertNode extends ContentNode {

  public final String value;

  public InsertNode(String value, CursorPosition position, List<AstNode> children, List<LetBinding> letBindings) {
    super(position, children, letBindings);

    this.value = value;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "InsertNode{\n" +
      indent(indentLevel + 1) + "value='" + value + "',\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
