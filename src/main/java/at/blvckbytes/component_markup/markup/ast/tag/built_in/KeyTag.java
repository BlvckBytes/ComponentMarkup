package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.KeyNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KeyTag extends TagDefinition {

  private static final String TAG_NAME = "key";

  private static final MandatoryExpressionAttributeDefinition ATTR_KEY = new MandatoryExpressionAttributeDefinition("key");

  public KeyTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL,
      ATTR_KEY
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
    return new KeyNode(ATTR_KEY.single(attributes), position, letBindings);
  }
}
