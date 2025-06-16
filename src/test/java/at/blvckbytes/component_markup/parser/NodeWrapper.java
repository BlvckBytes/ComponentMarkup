package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.control.ContainerNode;
import at.blvckbytes.component_markup.ast.node.style.Format;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.xml.event.CursorPositionEvent;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

public class NodeWrapper {

  private final AstNode node;

  public NodeWrapper(AstNode node) {
    this.node = node;
  }

  public NodeWrapper child(AstNode child) {
    assert node.children != null;
    node.children.add(child);
    return this;
  }

  public NodeWrapper let(String name, AExpression expression, TextWithAnchors text, int anchorIndex) {
    assert node.letBindings != null;
    node.letBindings.add(new LetBinding(name, expression, getAnchorPosition(text, anchorIndex)));
    return this;
  }

  public NodeWrapper color(String color) {
    return color(ImmediateExpression.of(color));
  }

  public NodeWrapper color(AExpression color) {
    if (!(node instanceof ContainerNode))
      throw new IllegalStateException("Can only style a container-node!");

    ((ContainerNode) node).style.color = color;
    return this;
  }

  public NodeWrapper font(String font) {
    return font(ImmediateExpression.of(font));
  }

  public NodeWrapper font(AExpression font) {
    if (!(node instanceof ContainerNode))
      throw new IllegalStateException("Can only style a container-node!");

    ((ContainerNode) node).style.font = font;
    return this;
  }

  public NodeWrapper format(Format format, AExpression value) {
    if (!(node instanceof ContainerNode))
      throw new IllegalStateException("Can only style a container-node!");

    ((ContainerNode) node).style.setFormat(format, value);
    return this;
  }

  public NodeWrapper format(Format format, @Nullable Boolean value) {
    if (!(node instanceof ContainerNode))
      throw new IllegalStateException("Can only style a container-node!");

    AExpression expressionValue;

    if (value == null)
      expressionValue = ImmediateExpression.ofNull();
    else
      expressionValue = ImmediateExpression.of(value);

    ((ContainerNode) node).style.setFormat(format, expressionValue);
    return this;
  }

  public AstNode get() {
    return node;
  }

  public static CursorPosition getAnchorPosition(TextWithAnchors text, int anchorIndex) {
    CursorPositionEvent positionEvent = text.getAnchor(anchorIndex);

    if (positionEvent == null)
      throw new IllegalStateException("Required anchor at index " + anchorIndex);

    return positionEvent.position;
  }
}
