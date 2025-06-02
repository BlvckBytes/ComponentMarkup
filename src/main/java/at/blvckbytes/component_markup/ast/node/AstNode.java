package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class AstNode {

  private static final String INDENT_WIDTH = " ";

  public final CursorPosition position;

  protected AstNode(CursorPosition position) {
    this.position = position;
  }

  public abstract String stringify(int indentLevel);

  public static String indent(int indentLevel) {
    return String.join("", Collections.nCopies(indentLevel, INDENT_WIDTH));
  }

  protected <T> String stringifyList(@Nullable List<T> list, Function<T, String> stringifier) {
    if (list == null)
      return "";

    StringBuilder result = new StringBuilder();

    for (int itemIndex = 0; itemIndex < list.size(); ++itemIndex) {
      result.append('\n').append(stringifier.apply(list.get(itemIndex)));

      if (itemIndex != list.size() - 1)
        result.append(',');
      else
        result.append('\n');
    }

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
