package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.click.InsertNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InsertTag extends TagDefinition {

  private static final String TAG_NAME = "insert";

  private static final MandatoryExpressionAttributeDefinition ATTR_VALUE = new MandatoryExpressionAttributeDefinition("value");

  public InsertTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL,
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
    return new InsertNode(
      ATTR_VALUE.single(attributes),
      position, children, letBindings
    );
  }
}
