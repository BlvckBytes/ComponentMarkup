package at.blvckbytes.component_markup.markup.ast.tag.attribute;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.StringView;

public class MarkupAttribute extends Attribute {

  public final MarkupNode value;

  public MarkupAttribute(StringView name, MarkupNode value) {
    super(name);

    this.value = value;
  }
}
