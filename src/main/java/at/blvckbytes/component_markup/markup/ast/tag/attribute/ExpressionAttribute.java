package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class ExpressionAttribute extends Attribute {

  public final ExpressionNode value;

  public ExpressionAttribute(
    String name,
    CursorPosition position,
    ExpressionNode value
  ) {
    super(name, position);

    this.value = value;
  }
}
