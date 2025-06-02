package at.blvckbytes.component_markup.ast.tag;

import at.blvckbytes.component_markup.ast.node.AstNode;
import me.blvckbytes.gpeee.parser.expression.AExpression;

public class LetBinding {

  public final String name;
  public final AExpression expression;

  public LetBinding(
    String name,
    AExpression expression
  ) {
    this.name = name;
    this.expression = expression;
  }

  public String stringify(int indentLevel) {
    return(
      AstNode.indent(indentLevel) + "LetBinding{\n" +
      AstNode.indent(indentLevel + 1) + "name='" + name + "',\n" +
      AstNode.indent(indentLevel + 1) + "expression='" + expression.expressionify() + "',\n" +
      AstNode.indent(indentLevel) + "}"
    );
  }
}
