package at.blvckbytes.component_markup.ast.tag.attribute;

import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;

public class ExpressionAttribute extends Attribute {

  public final AExpression value;

  public ExpressionAttribute(
    String name,
    CursorPosition position,
    AExpression value
  ) {
    super(name, position);

    this.value = value;
  }
}
