package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FormatTag extends TagDefinition {

  private static final String TAG_NAME = "format";

  private static final ExpressionAttributeDefinition ATTR_OBFUSCATED = new ExpressionAttributeDefinition("obfuscated");
  private static final ExpressionAttributeDefinition ATTR_STRIKETHROUGH = new ExpressionAttributeDefinition("strikethrough");
  private static final ExpressionAttributeDefinition ATTR_UNDERLINED = new ExpressionAttributeDefinition("underlined");
  private static final ExpressionAttributeDefinition ATTR_ITALIC = new ExpressionAttributeDefinition("italic");
  private static final ExpressionAttributeDefinition ATTR_BOLD = new ExpressionAttributeDefinition("bold");

  public FormatTag() {
    super(
      new String[] { TAG_NAME },
      TagClosing.OPEN_CLOSE,
      TagPriority.NORMAL,
      ATTR_OBFUSCATED,
      ATTR_STRIKETHROUGH,
      ATTR_UNDERLINED,
      ATTR_ITALIC,
      ATTR_BOLD
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
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    NodeStyle style = wrapper.getOrInstantiateStyle();

    ExpressionNode expression;

    if ((expression = ATTR_OBFUSCATED.singleOrNull(attributes)) != null)
      style.setFormat(Format.MAGIC, expression);

    if ((expression = ATTR_BOLD.singleOrNull(attributes)) != null)
      style.setFormat(Format.BOLD, expression);

    if ((expression = ATTR_STRIKETHROUGH.singleOrNull(attributes)) != null)
      style.setFormat(Format.STRIKETHROUGH, expression);

    if ((expression = ATTR_UNDERLINED.singleOrNull(attributes)) != null)
      style.setFormat(Format.UNDERLINED, expression);

    if ((expression = ATTR_ITALIC.singleOrNull(attributes)) != null)
      style.setFormat(Format.ITALIC, expression);

    return wrapper;
  }
}
