package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeAttributes;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeCharsNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeTag;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RainbowTag extends ColorizeTag {

  private static final String TAG_NAME = "rainbow";

  public RainbowTag() {
    super(
      new String[] { TAG_NAME },
      TagPriority.NORMAL
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return TAG_NAME.equals(tagNameLower);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ColorizeAttributes baseAttributes = getBaseAttributes(attributes);

    return new ColorizeCharsNode(
      tagNameLower,
      interpreter -> (
        new RainbowNodeState(
          tagNameLower,
          baseAttributes.getPhase(interpreter),
          baseAttributes.getFlags(interpreter)
        )
      ),
      position, children, letBindings
    );
  }
}
