package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;

import java.util.ArrayList;
import java.util.List;

public class MandatoryMarkupAttributeDefinition extends AttributeDefinition {

  public MandatoryMarkupAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, MarkupAttribute.class, flags);
  }

  public MarkupNode single(List<Attribute> attributes) {
    for (Attribute attribute : attributes) {
      if (!matches(attribute))
        continue;

      return ((MarkupAttribute) attribute).value;
    }

    throw new AbsentMandatoryAttributeException(this);
  }

  public List<MarkupNode> multi(List<Attribute> attributes) {
    List<MarkupNode> result = new ArrayList<>();

    for (Attribute attribute : attributes) {
      if (!matches(attribute))
        continue;

      result.add(((MarkupAttribute) attribute).value);
    }

    return result;
  }
}
