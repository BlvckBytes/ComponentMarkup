package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.expression.ImmediateExpression;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.node.style.Format;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionLetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

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

  public NodeWrapper<T> let(String name, ExpressionNode expression, CursorPosition position) {
    if(node.letBindings == null)
      node.letBindings = new HashSet<>();

    node.letBindings.add(new ExpressionLetBinding(expression, name, position));
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

  public NodeWrapper<T> color(String color) {
    return color(ImmediateExpression.of(color));
  }

  public NodeWrapper<T> color(ExpressionNode color) {
    if (!(node instanceof StyledNode))
      throw new IllegalStateException("The node " + node.getClass() + " cannot hold any styles");

    ((StyledNode) node).getOrInstantiateStyle().color = color;
    return this;
  }

  public NodeWrapper<T> font(String font) {
    return font(ImmediateExpression.of(font));
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

  public NodeWrapper<T> format(Format format, @Nullable Boolean value) {
    if (!(node instanceof StyledNode))
      throw new IllegalStateException("The node " + node.getClass() + " cannot hold any styles");

    ExpressionNode expressionValue;

    if (value == null)
      expressionValue = null;
    else
      expressionValue = ImmediateExpression.of(value);

    ((StyledNode) node).getOrInstantiateStyle().setFormat(format, expressionValue);
    return this;
  }

  public T get() {
    return node;
  }

}
