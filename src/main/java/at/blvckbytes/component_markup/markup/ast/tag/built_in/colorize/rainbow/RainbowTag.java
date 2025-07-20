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
import java.util.Set;

public class RainbowTag extends ColorizeTag {

  public RainbowTag() {
    super(TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("rainbow");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable Set<LetBinding> letBindings,
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
