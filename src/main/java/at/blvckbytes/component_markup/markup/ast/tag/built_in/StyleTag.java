package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class StyleTag extends TagDefinition {

  public StyleTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("style");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable Set<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(position, children, letBindings);
    NodeStyle style = wrapper.getOrInstantiateStyle();

    ExpressionNode expression;

    if ((expression = attributes.getOptionalExpressionNode("obfuscated")) != null)
      style.setFormat(Format.OBFUSCATED, expression);

    if ((expression = attributes.getOptionalExpressionNode("bold")) != null)
      style.setFormat(Format.BOLD, expression);

    if ((expression = attributes.getOptionalExpressionNode("strikethrough")) != null)
      style.setFormat(Format.STRIKETHROUGH, expression);

    if ((expression = attributes.getOptionalExpressionNode("underlined")) != null)
      style.setFormat(Format.UNDERLINED, expression);

    if ((expression = attributes.getOptionalExpressionNode("italic")) != null)
      style.setFormat(Format.ITALIC, expression);

    if ((expression = attributes.getOptionalExpressionNode("font")) != null)
      style.font = expression;

    if ((expression = attributes.getOptionalExpressionNode("color")) != null)
      style.color = expression;

    if ((expression = attributes.getOptionalExpressionNode("shadow")) != null)
      style.shadowColor = expression;

    if ((expression = attributes.getOptionalExpressionNode("shadow-opacity")) != null)
      style.shadowColorOpacity = expression;

    if ((expression = attributes.getOptionalExpressionNode("reset")) != null)
      style.reset = expression;

    return wrapper;
  }
}
