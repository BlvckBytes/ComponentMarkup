package at.blvckbytes.component_markup.markup.ast.tag.built_in.hover;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.hover.AchievementHoverNode;
import at.blvckbytes.component_markup.markup.ast.tag.AttributeDefinition;
import at.blvckbytes.component_markup.markup.ast.tag.AttributeType;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverAchievementTag extends HoverTag {

  public HoverAchievementTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("value", AttributeType.EXPRESSION, false, true)
      },
      "hover-achievement"
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
      findExpressionAttribute("value", attributes),
      position, children, letBindings
    );
  }
}
