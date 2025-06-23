package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScoreNode extends ContentNode {

  public final AExpression name;
  public final AExpression objective;
  public final @Nullable AExpression value;

  public ScoreNode(
    AExpression name,
    AExpression objective,
    @Nullable AExpression value,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.name = name;
    this.objective = objective;
    this.value = value;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "ScoreNode{\n" +
      indent(indentLevel + 1) + "name=" + name.expressionify() + ",\n" +
      indent(indentLevel + 1) + "objective=" + objective.expressionify() + ",\n" +
      indent(indentLevel + 1) + "value=" + (value == null ? "null" : value.expressionify()) + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
