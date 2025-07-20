package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.transition;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeAttributes;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeTag;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class TransitionTag extends ColorizeTag {

  public TransitionTag() {
    super(TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("transition");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable Set<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionList colors = attributes.getMandatoryExpressionList("color");
    ExpressionList offsets = attributes.getOptionalExpressionList("offset");
    ExpressionList zIndices = attributes.getOptionalExpressionList("z-index");
    ColorizeAttributes baseAttributes = getBaseAttributes(attributes);

    return new ColorizeMonochromeNode(
      tagNameLower,
      interpreter -> (
        new TransitionNodeState(
          tagNameLower,
          colors,
          offsets,
          zIndices,
          baseAttributes.getPhase(interpreter),
          baseAttributes.getFlags(interpreter),
          interpreter
        )
      ),
      position, children, letBindings
    );
  }
}
