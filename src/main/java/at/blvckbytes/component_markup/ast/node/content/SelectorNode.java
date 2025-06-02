package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectorNode extends ContentNode {

  public final AExpression selector;
  public final @Nullable AstNode separator;

  public SelectorNode(
    AExpression selector,
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
      indent(indentLevel + 1) + "selector=" + selector.expressionify() + ",\n" +
      stringifySubtree(separator, "separator", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
