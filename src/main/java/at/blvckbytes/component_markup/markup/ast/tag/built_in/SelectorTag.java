package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.SelectorNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectorTag extends TagDefinition {

  private static final String TAG_NAME = "selector";

  private static final MandatoryExpressionAttributeDefinition ATTR_SELECTOR = new MandatoryExpressionAttributeDefinition("selector");
  private static final MarkupAttributeDefinition ATTR_SEPARATOR = new MarkupAttributeDefinition("separator");

  public SelectorTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL,
      ATTR_SELECTOR,
      ATTR_SEPARATOR
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(TAG_NAME);
  }

  @Override
  public @NotNull MarkupNode createNode(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    return new SelectorNode(
      ATTR_SELECTOR.single(attributes),
      ATTR_SEPARATOR.singleOrNull(attributes),
      position, letBindings
    );
  }
}
