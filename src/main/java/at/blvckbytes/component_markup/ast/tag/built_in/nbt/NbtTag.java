package at.blvckbytes.component_markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.NbtNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;

import java.util.List;

public abstract class NbtTag extends TagDefinition {

  private final NbtSource source;

  protected NbtTag(NbtSource source) {
    this.source = source;
  }

  @Override
  public TagClosing getClosing() {
    return TagClosing.SELF_CLOSE;
  }

  @Override
  public TagPriority getPriority() {
    return TagPriority.NORMAL;
  }

  @Override
  public AttributeDefinition[] getAttributes() {
    return new AttributeDefinition[] {
      new AttributeDefinition(source.attributeName, AttributeType.STRING, false, true),
      new AttributeDefinition("path", AttributeType.STRING, false, true),
      new AttributeDefinition("interpret", AttributeType.BOOLEAN, false, false),
      new AttributeDefinition("separator", AttributeType.SUBTREE, false, false)
    };
  }

  @Override
  public AstNode construct(
    String tagName,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    Boolean interpret = tryGetBooleanAttribute("interpret", attributes);

    return new NbtNode(
      source,
      getStringAttribute(source.attributeName, attributes),
      getStringAttribute("path", attributes),
      interpret != null && interpret,
      tryGetSubtreeAttribute("separator", attributes),
      children,
      letBindings
    );
  }
}
