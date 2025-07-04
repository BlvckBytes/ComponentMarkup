package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.AchievementHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HoverAchievementTag extends HoverTag {

  private static final MandatoryExpressionAttributeDefinition ATTR_VALUE = new MandatoryExpressionAttributeDefinition("value");

  public HoverAchievementTag() {
    super(
      "hover-achievement",
      ATTR_VALUE
    );
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new AchievementHoverNode(
      ATTR_VALUE.single(attributes),
      position, children, letBindings
    );
  }
}
