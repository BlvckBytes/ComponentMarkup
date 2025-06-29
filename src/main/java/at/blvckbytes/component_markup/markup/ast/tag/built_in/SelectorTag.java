package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.content.SelectorNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectorTag extends TagDefinition {

  private static final String TAG_NAME = "selector";

  public SelectorTag() {
    super(
      new AttributeDefinition[] {
        new ExpressionAttributeDefinition("selector", AttributeFlag.MANDATORY),
        new MarkupAttributeDefinition("separator")
      },
      new String[] { TAG_NAME },
      TagClosing.SELF_CLOSE,
      TagPriority.NORMAL
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
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<MarkupNode> children
  ) {
    return new SelectorNode(
      findExpressionAttribute("selector", attributes),
      tryFindMarkupAttribute("separator", attributes),
      position, letBindings
    );
  }
}
