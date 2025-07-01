package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.ScoreNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScoreTag extends TagDefinition {

  private static final String TAG_NAME = "score";

  private static final MandatoryExpressionAttributeDefinition ATTR_NAME = new MandatoryExpressionAttributeDefinition("name");
  private static final MandatoryExpressionAttributeDefinition ATTR_OBJECTIVE = new MandatoryExpressionAttributeDefinition("objective");
  private static final ExpressionAttributeDefinition ATTR_VALUE = new ExpressionAttributeDefinition("value");

  public ScoreTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL,
      ATTR_NAME,
      ATTR_OBJECTIVE,
      ATTR_VALUE
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(TAG_NAME);
  }

  @Override
  public @NotNull MarkupNode construct(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    return new ScoreNode(
      ATTR_NAME.single(attributes),
      ATTR_OBJECTIVE.single(attributes),
      ATTR_VALUE.singleOrNull(attributes),
      position, letBindings
    );
  }
}
