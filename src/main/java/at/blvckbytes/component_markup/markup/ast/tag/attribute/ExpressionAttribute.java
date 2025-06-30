package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class ExpressionAttribute extends Attribute {

  public final ExpressionNode value;
  public final boolean isInSpreadMode;

  public ExpressionAttribute(
    String name,
    CursorPosition position,
    ExpressionNode value,
    boolean isInSpreadMode
  ) {
    super(name, position);

    this.value = value;
    this.isInSpreadMode = isInSpreadMode;
  }
}
