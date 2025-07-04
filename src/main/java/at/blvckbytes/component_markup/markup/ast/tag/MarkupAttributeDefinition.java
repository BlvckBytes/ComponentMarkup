package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MarkupAttributeDefinition extends AttributeDefinition {

  private static final List<MarkupNode> EMPTY_LIST = Collections.emptyList();

  public MarkupAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, MarkupAttribute.class, flags);
  }

  public @Nullable MarkupNode singleOrNull(@Nullable AttributeMap attributes) {
    if (attributes == null)
      return null;

    return attributes.firstMarkupOrNull(this);
  }

  public List<MarkupNode> multi(@Nullable AttributeMap attributes) {
    if (attributes == null)
      return EMPTY_LIST;

    return attributes.markups(this);
  }
}
