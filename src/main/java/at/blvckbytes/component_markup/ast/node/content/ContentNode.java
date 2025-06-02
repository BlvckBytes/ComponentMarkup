package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ContentNode extends AstNode {

  public final NodeStyle style;
  public final @Nullable List<AstNode> children;
  public final List<LetBinding> letBindings;

  public ContentNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position);

    this.style = new NodeStyle();
    this.children = children;
    this.letBindings = letBindings;
  }

  // Convenience-method used when specifying ASTs in test-cases
  public ContentNode getStyle(Consumer<NodeStyle> handler) {
    handler.accept(this.style);
    return this;
  }

  protected String stringifyBaseMembers(int indentLevel) {
    return (
      indent(indentLevel) + "position=" + position + ",\n" +
      indent(indentLevel) + "children=" + stringifyList(children, indentLevel) + ",\n" +
      indent(indentLevel) + "letBindings=" + stringifyList(letBindings, indentLevel) + ",\n" +
      indent(indentLevel) + "style=(\n" +
      style.stringify(indentLevel + 1) + "\n" +
      indent(indentLevel) + ")"
    );
  }
}
