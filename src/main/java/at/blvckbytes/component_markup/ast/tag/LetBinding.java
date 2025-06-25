package at.blvckbytes.component_markup.ast.tag;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;

public class LetBinding extends Jsonifiable {

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
}
