package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeAttributes;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeCharsNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeTag;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GradientTag extends ColorizeTag {

  private static final MandatoryExpressionAttributeDefinition ATTR_COLOR = new MandatoryExpressionAttributeDefinition("color", AttributeFlag.MULTI_VALUE);
  private static final ExpressionAttributeDefinition ATTR_OFFSET = new ExpressionAttributeDefinition("offset", AttributeFlag.MULTI_VALUE);
  private static final ExpressionAttributeDefinition ATTR_Z_INDEX = new ExpressionAttributeDefinition("z-index", AttributeFlag.MULTI_VALUE);

  public GradientTag() {
    super(
      TagPriority.NORMAL,
      ATTR_COLOR,
      ATTR_OFFSET,
      ATTR_Z_INDEX
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("gradient");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ExpressionList colors = ATTR_COLOR.multi(attributes);
    ExpressionList offsets = ATTR_OFFSET.multi(attributes);
    ExpressionList zIndices = ATTR_Z_INDEX.multi(attributes);
    ColorizeAttributes baseAttributes = getBaseAttributes(attributes);

    return new ColorizeCharsNode(
      tagNameLower,
      interpreter -> (
        new GradientNodeState(
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
