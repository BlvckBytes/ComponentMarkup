package at.blvckbytes.component_markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.node.content.NbtNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class NbtTag extends TagDefinition {

  private final NbtSource source;
  private final String tagName;

  protected NbtTag(NbtSource source, String tagName) {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition(source.attributeName, AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("path", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("interpret", AttributeType.EXPRESSION, false, false),
        new AttributeDefinition("separator", AttributeType.SUBTREE, false, false)
      },
      new String[] { tagName },
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL
    );

    this.source = source;
    this.tagName = tagName;
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(this.tagName);
  }

  @Override
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
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
