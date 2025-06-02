package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.built_in.click.ClickAction;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class ClickNode extends ContentNode {

  public final ClickAction action;
  public final String value;

  public ClickNode(ClickAction action, String value, CursorPosition position, List<AstNode> children, List<LetBinding> letBindings) {
    super(position, children, letBindings);

    this.action = action;
    this.value = value;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "ClickNode{\n" +
      indent(indentLevel + 1) + "action=" + action.name() + ",\n" +
      indent(indentLevel + 1) + "value='" + value + "',\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
