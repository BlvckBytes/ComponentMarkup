package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import org.jetbrains.annotations.Nullable;

public class ExpressionAttributeDefinition extends AttributeDefinition {

  private static final ExpressionList EMPTY_LIST = new ExpressionList();

  public ExpressionAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, ExpressionAttribute.class, flags);
  }

  public @Nullable ExpressionNode singleOrNull(@Nullable AttributeMap attributes) {
    if (attributes == null)
      return null;

    return attributes.firstExpressionOrNull(this);
  }

  public ExpressionList multi(@Nullable AttributeMap attributes) {
    if (attributes == null)
      return EMPTY_LIST;

    return attributes.expressions(this);
  }
}
