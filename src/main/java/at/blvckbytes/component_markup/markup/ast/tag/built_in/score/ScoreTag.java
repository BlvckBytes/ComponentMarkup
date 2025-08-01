package at.blvckbytes.component_markup.markup.ast.tag.built_in.score;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class ScoreTag extends TagDefinition {

  public ScoreTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals("score", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new ScoreNode(
      attributes.getMandatoryExpressionNode("name"),
      attributes.getMandatoryExpressionNode("objective"),
      attributes.getOptionalExpressionNode("value"),
      tagName, letBindings
    );
  }
}
