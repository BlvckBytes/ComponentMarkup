package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;

import java.util.List;

public class MandatoryExpressionAttributeDefinition extends AttributeDefinition {

  public MandatoryExpressionAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, ExpressionAttribute.class, flags);
  }

  public ExpressionNode single(AttributeMap attributes) {
    ExpressionNode result = attributes.firstExpressionOrNull(name);

    if (result != null)
      return result;

    throw new AbsentMandatoryAttributeException(this);
  }

  public List<ExpressionNode> multi(AttributeMap attributes) {
    return attributes.expressions(name);
  }
}
