package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeAttributes;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeTag;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GradientTag extends ColorizeTag {

  private static final String TAG_NAME = "gradient";

  private static final MandatoryExpressionAttributeDefinition ATTR_COLOR = new MandatoryExpressionAttributeDefinition("color", AttributeFlag.MULTI_VALUE);
  private static final MandatoryExpressionAttributeDefinition ATTR_OFFSET = new MandatoryExpressionAttributeDefinition("offset", AttributeFlag.MULTI_VALUE);
  private static final MandatoryExpressionAttributeDefinition ATTR_Z_INDEX = new MandatoryExpressionAttributeDefinition("z-index", AttributeFlag.MULTI_VALUE);

  public GradientTag() {
    super(
      new String[] { TAG_NAME },
      TagPriority.NORMAL,
      ATTR_COLOR,
      ATTR_OFFSET,
      ATTR_Z_INDEX
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return TAG_NAME.equals(tagNameLower);
  }

  @Override
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    ExpressionList colors = ATTR_COLOR.multi(attributes);
    ExpressionList offsets = ATTR_OFFSET.multi(attributes);
    ExpressionList zIndices = ATTR_Z_INDEX.multi(attributes);
    ColorizeAttributes baseAttributes = getBaseAttributes(attributes);

    return new ColorizeNode(
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
