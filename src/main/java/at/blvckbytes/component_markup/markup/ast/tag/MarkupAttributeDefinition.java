package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MarkupAttributeDefinition extends AttributeDefinition {

  public MarkupAttributeDefinition(String name, AttributeFlag... flags) {
    super(name, MarkupAttribute.class, flags);
  }

  public @Nullable MarkupNode singleOrNull(AttributeMap attributes) {
    return attributes.firstMarkupOrNull(name);
  }

  public List<MarkupNode> multi(AttributeMap attributes) {
    return attributes.markups(name);
  }
}
