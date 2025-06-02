package at.blvckbytes.component_markup.ast.tag;

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
}
