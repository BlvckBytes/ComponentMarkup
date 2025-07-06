package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.ScoreNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreTag extends TagDefinition {

  private static final MandatoryExpressionAttributeDefinition ATTR_NAME = new MandatoryExpressionAttributeDefinition("name");
  private static final MandatoryExpressionAttributeDefinition ATTR_OBJECTIVE = new MandatoryExpressionAttributeDefinition("objective");
  private static final ExpressionAttributeDefinition ATTR_VALUE = new ExpressionAttributeDefinition("value");

  public ScoreTag() {
    super(
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL,
      ATTR_NAME,
      ATTR_OBJECTIVE,
      ATTR_VALUE
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("score");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new ScoreNode(
      ATTR_NAME.single(attributes),
      ATTR_OBJECTIVE.single(attributes),
      ATTR_VALUE.singleOrNull(attributes),
      position, letBindings
    );
  }
}
