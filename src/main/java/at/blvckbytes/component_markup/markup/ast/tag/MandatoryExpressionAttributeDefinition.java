package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import org.jetbrains.annotations.Nullable;

public class MandatoryExpressionAttributeDefinition extends AttributeDefinition {

  public MandatoryExpressionAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, ExpressionAttribute.class, flags);
  }

  public ExpressionNode single(@Nullable AttributeMap attributes) {
    if (attributes == null)
      throw new AbsentMandatoryAttributeException(this);

    ExpressionNode result = attributes.firstExpressionOrNull(this);

    if (result != null)
      return result;

    throw new AbsentMandatoryAttributeException(this);
  }

  public ExpressionList multi(@Nullable AttributeMap attributes) {
    if (attributes == null)
      throw new AbsentMandatoryAttributeException(this);

    return attributes.expressions(this);
  }
}
