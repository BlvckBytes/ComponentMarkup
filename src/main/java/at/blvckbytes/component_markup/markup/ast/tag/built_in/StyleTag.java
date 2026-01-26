/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class StyleTag extends TagDefinition {

  public StyleTag() {
    super(TagClosing.OPEN_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(InputView tagName) {
    return tagName.contentEquals("style", true);
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull InputView tagName,
    boolean selfClosing,
    @NotNull AttributeMap attributes,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    ContainerNode wrapper = new ContainerNode(tagName, children, letBindings);
    NodeStyle style = wrapper.getOrInstantiateStyle();

    ExpressionNode expression;

    if ((expression = attributes.getOptionalExpressionNode("obfuscated", "obf")) != null)
      style.setFormat(Format.OBFUSCATED, expression);

    if ((expression = attributes.getOptionalExpressionNode("bold", "b")) != null)
      style.setFormat(Format.BOLD, expression);

    if ((expression = attributes.getOptionalExpressionNode("strikethrough", "st")) != null)
      style.setFormat(Format.STRIKETHROUGH, expression);

    if ((expression = attributes.getOptionalExpressionNode("underlined", "u")) != null)
      style.setFormat(Format.UNDERLINED, expression);

    if ((expression = attributes.getOptionalExpressionNode("italic", "i")) != null)
      style.setFormat(Format.ITALIC, expression);

    if ((expression = attributes.getOptionalExpressionNode("font")) != null)
      style.font = expression;

    if ((expression = attributes.getOptionalExpressionNode("color", "c")) != null)
      style.color = expression;

    if ((expression = attributes.getOptionalExpressionNode("shadow")) != null)
      style.shadowColor = expression;

    if ((expression = attributes.getOptionalExpressionNode("shadow-opacity")) != null)
      style.shadowColorOpacity = expression;

    if ((expression = attributes.getOptionalExpressionNode("reset", "r")) != null)
      style.reset = expression;

    return wrapper;
  }
}
