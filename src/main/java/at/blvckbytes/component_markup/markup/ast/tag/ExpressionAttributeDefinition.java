package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExpressionAttributeDefinition extends AttributeDefinition {

  public ExpressionAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, ExpressionAttribute.class, flags);
  }

  public @Nullable ExpressionNode singleOrNull(AttributeMap attributes) {
    return attributes.firstExpressionOrNull(name);
  }

  public List<ExpressionNode> multi(AttributeMap attributes) {
    return attributes.expressions(name);
  }
}
