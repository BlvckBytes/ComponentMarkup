package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;

public class KeyNode extends AstNode {

  public final AExpression key;

  public KeyNode(
    AExpression key,
    CursorPosition position,
    List<LetBinding> letBindings
  ) {
    super(position, null, letBindings);

    this.key = key;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "KeyNode{\n" +
      indent(indentLevel + 1) + "key=" + key.expressionify() + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
