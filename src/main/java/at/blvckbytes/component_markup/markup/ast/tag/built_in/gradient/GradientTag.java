package at.blvckbytes.component_markup.markup.ast.tag.built_in.gradient;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GradientTag extends TagDefinition {

  private static final String TAG_NAME = "gradient";

  private static final MandatoryExpressionAttributeDefinition ATTR_COLOR = new MandatoryExpressionAttributeDefinition("color", AttributeFlag.MULTI_VALUE);
  private static final MandatoryExpressionAttributeDefinition ATTR_OFFSET = new MandatoryExpressionAttributeDefinition("offset", AttributeFlag.MULTI_VALUE);
  private static final ExpressionAttributeDefinition ATTR_DEEP = new ExpressionAttributeDefinition("deep");

  public GradientTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL,
      ATTR_COLOR,
      ATTR_OFFSET,
      ATTR_DEEP
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
    return new GradientNode(
      ATTR_COLOR.multi(attributes),
      ATTR_OFFSET.multi(attributes),
      ATTR_DEEP.singleOrNull(attributes),
      position, children, letBindings
    );
  }
}
