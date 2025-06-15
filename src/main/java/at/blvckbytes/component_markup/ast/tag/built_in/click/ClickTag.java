package at.blvckbytes.component_markup.ast.tag.built_in.click;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.click.ClickNode;
import at.blvckbytes.component_markup.ast.tag.*;
import at.blvckbytes.component_markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public abstract class ClickTag extends TagDefinition  {

  private final ClickAction action;
  private final String tagName;

  protected ClickTag(ClickAction action, String tagName) {
    super(
      new AttributeDefinition[] {
        new AttributeDefinition("value", AttributeType.EXPRESSION, false, true)
      },
      new String[] { tagName }
    );

    this.tagName = tagName;
    this.action = action;
  }

  @Override
  public boolean matchName(String tagName) {
    return tagName.equals(this.tagName);
  }

  @Override
  public TagClosing getClosing() {
    return TagClosing.OPEN_CLOSE;
  }

  @Override
  public TagPriority getPriority() {
    return TagPriority.NORMAL;
  }

  @Override
  public AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  ) {
    return new ClickNode(
      action,
      findExpressionAttribute("value", attributes),
      position, children, letBindings
    );
  }
}
