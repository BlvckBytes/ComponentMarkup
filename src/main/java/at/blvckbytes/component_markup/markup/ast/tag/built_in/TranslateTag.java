package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.TranslateNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TranslateTag extends TagDefinition {

  private static final String TAG_NAME = "translate";

  private static final MandatoryExpressionAttributeDefinition ATTR_KEY = new MandatoryExpressionAttributeDefinition("key");
  private static final MarkupAttributeDefinition ATTR_WITH = new MarkupAttributeDefinition("with", AttributeFlag.MULTI_VALUE);
  private static final ExpressionAttributeDefinition ATTR_FALLBACK = new ExpressionAttributeDefinition("fallback");

  public TranslateTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL,
      ATTR_KEY,
      ATTR_WITH,
      ATTR_FALLBACK
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(TAG_NAME);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new TranslateNode(
      ATTR_KEY.single(attributes),
      ATTR_WITH.multi(attributes),
      ATTR_FALLBACK.singleOrNull(attributes),
      position, letBindings
    );
  }
}
