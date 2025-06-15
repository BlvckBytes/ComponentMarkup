package at.blvckbytes.component_markup.ast.tag.built_in;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.content.ScoreNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreTag extends TagDefinition {

  private static final String TAG_NAME = "score";

  public ScoreTag() {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("name", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("objective", AttributeType.EXPRESSION, false, true),
        new AttributeDefinition("value", AttributeType.EXPRESSION, false, false)
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
  public @Nullable AstNode construct(
    String tagNameLower,
    boolean didModifyContainer,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new ScoreNode(
      findExpressionAttribute("name", attributes),
      findExpressionAttribute("objective", attributes),
      tryFindExpressionAttribute("value", attributes),
      position, letBindings
    );
  }
}
