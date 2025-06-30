package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;

import java.util.List;

public class MandatoryMarkupAttributeDefinition extends AttributeDefinition {

  public MandatoryMarkupAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, MarkupAttribute.class, flags);
  }

  public MarkupNode single(AttributeMap attributes) {
    MarkupNode result = attributes.firstMarkupOrNull(this);

    if (result != null)
      return result;

    throw new AbsentMandatoryAttributeException(this);
  }

  public List<MarkupNode> multi(AttributeMap attributes) {
    return attributes.markups(this);
  }
}
