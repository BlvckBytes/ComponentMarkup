package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.TranslateNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TranslateTag extends TagDefinition {

  private static final String TAG_NAME = "translate";

  public TranslateTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("key", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("with", AttributeType.SUBTREE, true, false),
        new AttributeDefinition("fallback", AttributeType.SUBTREE, false, false)
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
  public @NotNull AstNode construct(
    String tagNameLower,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new TranslateNode(
      findExpressionAttribute("key", attributes),
      findSubtreeAttributes("with", attributes),
      tryFindSubtreeAttribute("fallback", attributes),
      position, letBindings
    );
  }
}
