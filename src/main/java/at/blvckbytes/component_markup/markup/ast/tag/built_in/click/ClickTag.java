package at.blvckbytes.component_markup.markup.ast.tag.built_in.click;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public abstract class ClickTag extends TagDefinition {

  private final ClickAction action;
  private final String tagName;

  protected ClickTag(ClickAction action, String tagName) {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);

    this.tagName = tagName;
    this.action = action;
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals(this.tagName, true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull StringPosition position,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new ClickNode(
      action,
      attributes.getMandatoryExpressionNode("value"),
      position, children, letBindings
    );
  }
}
