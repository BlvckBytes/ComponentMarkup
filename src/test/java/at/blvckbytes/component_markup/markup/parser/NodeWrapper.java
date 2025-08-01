/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionLetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringView;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class NodeWrapper<T extends MarkupNode> {

  private final T node;

  public NodeWrapper(T node) {
    this.node = node;
  }

  public NodeWrapper<T> child(NodeWrapper<?> wrappedChild) {
    if(node.children == null)
      node.children = new ArrayList<>();

    node.children.add(wrappedChild.get());
    return this;
  }

  public NodeWrapper<T> let(StringView name, ExpressionNode expression) {
    if(node.letBindings == null)
      node.letBindings = new LinkedHashSet<>();

    node.letBindings.add(new ExpressionLetBinding(expression, false, name));
    return this;
  }

  public NodeWrapper<T> ifCondition(ExpressionNode condition) {
    node.ifCondition = condition;
    return this;
  }

  public NodeWrapper<T> useCondition(ExpressionNode condition) {
    node.ifCondition = condition;
    return this;
  }

  public NodeWrapper<T> color(StringView color) {
    return color(ImmediateExpression.ofString(color, color.buildString()));
  }

  public NodeWrapper<T> color(ExpressionNode color) {
    if (!(node instanceof StyledNode))
      throw new IllegalStateException("The node " + node.getClass() + " cannot hold any styles");

    ((StyledNode) node).getOrInstantiateStyle().color = color;
    return this;
  }

  public NodeWrapper<T> font(StringView font) {
    return font(ImmediateExpression.ofString(font, font.buildString()));
  }

  public NodeWrapper<T> font(ExpressionNode font) {
    if (!(node instanceof StyledNode))
      throw new IllegalStateException("The node " + node.getClass() + " cannot hold any styles");

    ((StyledNode) node).getOrInstantiateStyle().font = font;
    return this;
  }

  public NodeWrapper<T> format(Format format, ExpressionNode value) {
    if (!(node instanceof StyledNode))
      throw new IllegalStateException("The node " + node.getClass() + " cannot hold any styles");

    ((StyledNode) node).getOrInstantiateStyle().setFormat(format, value);
    return this;
  }

  public T get() {
    return node;
  }
}
