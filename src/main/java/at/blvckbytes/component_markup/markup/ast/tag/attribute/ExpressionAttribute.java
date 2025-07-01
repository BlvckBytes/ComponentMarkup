package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;

public class ExpressionAttribute extends Attribute {

  public final ExpressionNode value;
  public final boolean isInSpreadMode;

  public ExpressionAttribute(String name, ExpressionNode value, boolean isInSpreadMode) {
    super(name);

    this.value = value;
    this.isInSpreadMode = isInSpreadMode;
  }
}
