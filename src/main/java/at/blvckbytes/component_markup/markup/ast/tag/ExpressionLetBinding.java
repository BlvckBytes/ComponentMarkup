package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class ExpressionLetBinding extends LetBinding {

  public final ExpressionNode expression;

  public ExpressionLetBinding(
    ExpressionNode expression,
    String name,
    CursorPosition position
  ) {
    super(name, position);
    this.expression = expression;
  }
}
