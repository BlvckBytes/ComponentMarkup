package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectorNode extends ContentNode {

  public final String selector;
  public final @Nullable AstNode separator;

  public SelectorNode(
    String selector,
    @Nullable AstNode separator,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.selector = selector;
    this.separator = separator;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "SelectorNode{\n" +
      indent(indentLevel + 1) + "selector='" + selector + "',\n" +
      stringifySubtree(separator, "separator", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
