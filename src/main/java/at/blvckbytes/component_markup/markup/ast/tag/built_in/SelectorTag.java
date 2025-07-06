package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.SelectorNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectorTag extends TagDefinition {

  private static final MandatoryExpressionAttributeDefinition ATTR_SELECTOR = new MandatoryExpressionAttributeDefinition("selector");
  private static final MarkupAttributeDefinition ATTR_SEPARATOR = new MarkupAttributeDefinition("separator");

  public SelectorTag() {
    super(
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL,
      ATTR_SELECTOR,
      ATTR_SEPARATOR
    );
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("selector");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @Nullable AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new SelectorNode(
      ATTR_SELECTOR.single(attributes),
      ATTR_SEPARATOR.singleOrNull(attributes),
      position, letBindings
    );
  }
}
