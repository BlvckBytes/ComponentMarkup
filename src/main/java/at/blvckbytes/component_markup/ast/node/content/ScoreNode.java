package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreNode extends AstNode {

  public final AExpression name;
  public final AExpression object;
  public final @Nullable AExpression value;

  public ScoreNode(
    AExpression name,
    AExpression object,
    @Nullable AExpression value,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.name = name;
    this.object = object;
    this.value = value;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "ScoreNode{\n" +
      indent(indentLevel + 1) + "name=" + name.expressionify() + ",\n" +
      indent(indentLevel + 1) + "object=" + object.expressionify() + ",\n" +
      indent(indentLevel + 1) + "value=" + (value == null ? "null" : value.expressionify()) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
