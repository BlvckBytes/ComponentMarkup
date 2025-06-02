package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class InterpolationNode extends ContentNode {

  public final AExpression expression;

  public InterpolationNode(
    AExpression expression,
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, letBindings);

    this.expression = expression;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "InterpolationNode{\n" +
      indent(indentLevel + 1) + "expression=" + expression.expressionify() + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
