package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class ContainerNode extends AstNode {

  public ContainerNode(CursorPosition position, List<AstNode> children, List<LetBinding> letBindings) {
    super(position, children, letBindings);
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "ContainerNode{\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
