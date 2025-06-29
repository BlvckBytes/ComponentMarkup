package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;

import java.util.ArrayList;
import java.util.List;

public class MandatoryExpressionAttributeDefinition extends AttributeDefinition {

  public MandatoryExpressionAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, ExpressionAttribute.class, flags);
  }

  public ExpressionNode single(List<Attribute> attributes) {
    for (Attribute attribute : attributes) {
      if (!matches(attribute))
        continue;

      return ((ExpressionAttribute) attribute).value;
    }

    throw new IllegalStateException("Did not receive mandatory expression-attribute " + name);
  }

  public List<ExpressionNode> multi(List<Attribute> attributes) {
    List<ExpressionNode> result = new ArrayList<>();

    for (Attribute attribute : attributes) {
      if (!matches(attribute))
        continue;

      result.add(((ExpressionAttribute) attribute).value);
    }

    return result;
  }
}
