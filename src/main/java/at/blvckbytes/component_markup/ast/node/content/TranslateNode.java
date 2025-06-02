package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TranslateNode extends AstNode {

  public final AExpression key;
  public final List<AstNode> with;
  public final @Nullable AstNode fallback;

  public TranslateNode(
    AExpression key,
    List<AstNode> with,
    @Nullable AstNode fallback,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.key = key;
    this.with = with;
    this.fallback = fallback;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "TranslateNode{\n" +
      indent(indentLevel + 1) + "key=" + key.expressionify() + ",\n" +
      indent(indentLevel + 1) + "with=" + stringifyList(with, indentLevel) + ",\n" +
      stringifySubtree(fallback, "fallback", indentLevel + 1) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
