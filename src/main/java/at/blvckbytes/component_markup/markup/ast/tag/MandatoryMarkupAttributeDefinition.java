package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MandatoryMarkupAttributeDefinition extends AttributeDefinition {

  public MandatoryMarkupAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, flags);
  }

  public MarkupNode single(@Nullable AttributeMap attributes) {
    if (attributes == null)
      throw new AbsentMandatoryAttributeException(this);

    MarkupNode result = attributes.firstMarkupOrNull(this);

    if (result != null)
      return result;

    throw new AbsentMandatoryAttributeException(this);
  }

  public List<MarkupNode> multi(@Nullable AttributeMap attributes) {
    if (attributes == null)
      throw new AbsentMandatoryAttributeException(this);

    return attributes.markups(this);
  }
}
