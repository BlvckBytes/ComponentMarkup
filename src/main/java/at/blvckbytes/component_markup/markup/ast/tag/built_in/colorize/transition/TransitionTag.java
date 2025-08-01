package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.transition;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeAttributes;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeTag;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class TransitionTag extends ColorizeTag {

  public TransitionTag() {
    super(TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(StringView tagName) {
    return tagName.contentEquals("transition", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull StringView tagName,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionList colors = attributes.getMandatoryExpressionList("color");
    ExpressionList offsets = attributes.getOptionalExpressionList("offset");
    ExpressionList zIndices = attributes.getOptionalExpressionList("z-index");
    ColorizeAttributes baseAttributes = getBaseAttributes(attributes);

    return new ColorizeMonochromeNode(
      tagName,
      interpreter -> (
        new TransitionNodeState(
          tagName,
          colors,
          offsets,
          zIndices,
          baseAttributes.getPhase(interpreter),
          baseAttributes.getFlags(interpreter),
          interpreter
        )
      ),
      tagName, children, letBindings
    );
  }
}
