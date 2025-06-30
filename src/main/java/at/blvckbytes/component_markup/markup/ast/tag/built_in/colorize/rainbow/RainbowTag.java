package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeAttributes;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeTag;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

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
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    ColorizeAttributes baseAttributes = getBaseAttributes(attributes);

    return new ColorizeNode(
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
