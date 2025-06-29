package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.AchievementHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

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
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    return new AchievementHoverNode(
      ATTR_VALUE.single(attributes),
      position, children, letBindings
    );
  }
}
