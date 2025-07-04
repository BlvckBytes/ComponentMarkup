package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.NbtNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class NbtTag extends TagDefinition {

  protected static final MandatoryExpressionAttributeDefinition ATTR_PATH = new MandatoryExpressionAttributeDefinition("path");
  protected static final ExpressionAttributeDefinition ATTR_INTERPRET = new ExpressionAttributeDefinition("interpret");
  protected static final MarkupAttributeDefinition ATTR_SEPARATOR = new MarkupAttributeDefinition("separator");

  private final NbtSource source;
  private final String tagName;
  private final MandatoryExpressionAttributeDefinition sourceAttribute;

  protected NbtTag(NbtSource source, String tagName, MandatoryExpressionAttributeDefinition sourceAttribute) {
    super(
      new String[] { tagName },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL,
      ATTR_PATH,
      ATTR_INTERPRET,
      ATTR_SEPARATOR,
      sourceAttribute
    );

    this.source = source;
    this.tagName = tagName;
    this.sourceAttribute = sourceAttribute;
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(this.tagName);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new NbtNode(
      source,
      sourceAttribute.single(attributes),
      ATTR_PATH.single(attributes),
      ATTR_INTERPRET.singleOrNull(attributes),
      ATTR_SEPARATOR.singleOrNull(attributes),
      position, letBindings
    );
  }
}
