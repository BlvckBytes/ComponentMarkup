package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public abstract class NbtTag extends TagDefinition {

  private final NbtSource source;
  private final String tagName;
  private final String sourceAttributeName;

  protected NbtTag(NbtSource source, String tagName, String sourceAttributeName) {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);

    this.source = source;
    this.tagName = tagName;
    this.sourceAttributeName = sourceAttributeName;
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals(this.tagName, true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new NbtNode(
      source,
      attributes.getMandatoryExpressionNode(sourceAttributeName),
      attributes.getMandatoryExpressionNode("path"),
      attributes.getOptionalExpressionNode("interpret"),
      attributes.getOptionalMarkupNode("separator"),
      tagName.startInclusive, letBindings
    );
  }
}
