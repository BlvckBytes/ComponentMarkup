package at.blvckbytes.component_markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.NbtNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

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
      new AttributeDefinition(source.attributeName, AttributeType.EXPRESSION, false, true),
      new AttributeDefinition("path", AttributeType.EXPRESSION, false, true),
      new AttributeDefinition("interpret", AttributeType.EXPRESSION, false, false),
      new AttributeDefinition("separator", AttributeType.SUBTREE, false, false)
    };
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new NbtNode(
      source,
      findExpressionAttribute(source.attributeName, attributes),
      findExpressionAttribute("path", attributes),
      tryFindExpressionAttribute("interpret", attributes),
      tryFindSubtreeAttribute("separator", attributes),
      position, letBindings
    );
  }
}
