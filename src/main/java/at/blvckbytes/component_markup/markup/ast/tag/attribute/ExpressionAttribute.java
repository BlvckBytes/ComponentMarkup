package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class ExpressionAttribute extends Attribute {

  public final ExpressionNode value;
  public final boolean isInSpreadMode;

  public ExpressionAttribute(CursorPosition position, String name, ExpressionNode value, boolean isInSpreadMode) {
    super(position, name);

    this.value = value;
    this.isInSpreadMode = isInSpreadMode;
  }
}
