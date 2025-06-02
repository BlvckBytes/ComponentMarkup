package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class AstNode {

  private static final String INDENT_WIDTH = " ";

  public final CursorPosition position;
  public final NodeStyle style;
  public final @Nullable List<AstNode> children;
  public final List<LetBinding> letBindings;

  public AstNode(
    CursorPosition position,
    @Nullable List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    this.position = position;
    this.style = new NodeStyle();
    this.children = children;
    this.letBindings = letBindings;
  }

  public abstract String stringify(int indentLevel);

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

  public static String indent(int indentLevel) {
    return String.join("", Collections.nCopies(indentLevel, INDENT_WIDTH));
  }

  protected String stringifyList(@Nullable List<?> list, int outerIndentLevel) {
    if (list == null || list.isEmpty())
      return "[]";

    StringBuilder result = new StringBuilder("[");

    for (int itemIndex = 0; itemIndex < list.size(); ++itemIndex) {
      result.append('\n');

      Object item = list.get(itemIndex);

      if (item instanceof AstNode)
        result.append(((AstNode) item).stringify(outerIndentLevel + 1));
      else if (item instanceof LetBinding)
        result.append(((LetBinding) item).stringify(outerIndentLevel + 1));
      else {
        if (item == null)
          result.append(indent(outerIndentLevel + 1)).append("null");

        else
          throw new IllegalStateException("Don't know how to stringify " + item.getClass().getSimpleName());
      }

      if (itemIndex != list.size() - 1)
        result.append(',');
    }

    result.append('\n').append(indent(outerIndentLevel)).append(']');

    return result.toString();
  }

  protected String stringifySubtree(@Nullable AstNode node, String key, int indentLevel) {
    return (
      indent(indentLevel + 1) + key + "=(\n" +
        (node == null
          ? indent(indentLevel + 2) + "null"
          : node.stringify(indentLevel + 2)
        ) + "\n" +
        indent(indentLevel + 1) + ")"
    );
  }
}
