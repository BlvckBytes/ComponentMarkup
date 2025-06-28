package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class LetBinding extends Jsonifiable {

  public final String name;
  public final ExpressionNode expression;
  public final CursorPosition position;

  public LetBinding(
    String name,
    ExpressionNode expression,
    CursorPosition position
  ) {
    this.name = name;
    this.expression = expression;
    this.position = position;
  }
}
