package at.blvckbytes.component_markup.ast.tag;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;

public class LetBinding {

  public final String name;
  public final AExpression expression;
  public final CursorPosition position;

  public LetBinding(
    String name,
    AExpression expression,
    CursorPosition position
  ) {
    this.name = name;
    this.expression = expression;
    this.position = position;
  }

  public String stringify(int indentLevel) {
    return(
      AstNode.indent(indentLevel) + "LetBinding{\n" +
      AstNode.indent(indentLevel + 1) + "name='" + name + "',\n" +
      AstNode.indent(indentLevel + 1) + "expression='" + expression.expressionify() + "',\n" +
      AstNode.indent(indentLevel + 1) + "position=" + position + ",\n" +
      AstNode.indent(indentLevel) + "}"
    );
  }
}
