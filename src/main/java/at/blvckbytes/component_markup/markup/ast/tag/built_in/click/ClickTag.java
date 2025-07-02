package at.blvckbytes.component_markup.markup.ast.tag.built_in.click;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ClickTag extends TagDefinition {

  private static final MandatoryExpressionAttributeDefinition ATTR_VALUE = new MandatoryExpressionAttributeDefinition("value");

  private final ClickAction action;
  private final String tagName;

  protected ClickTag(ClickAction action, String tagName) {
    super(
      new String[] { tagName },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL,
      ATTR_VALUE
    );

    this.tagName = tagName;
    this.action = action;
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals(this.tagName);
  }

  @Override
  public @NotNull MarkupNode createNode(
    String tagNameLower,
    CursorPosition position,
    AttributeMap attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    return new ClickNode(
      action,
      ATTR_VALUE.single(attributes),
      position, children, letBindings
    );
  }
}
