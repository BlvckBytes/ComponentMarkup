package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.parser.AttributeName;

public class ExpressionAttribute extends Attribute {

  public final AttributeName name;
  public final ExpressionNode value;

  public ExpressionAttribute(
    AttributeName name,
    ExpressionNode value
  ) {
    super(name.finalName);

    this.value = value;
    this.name = name;
  }
}
