package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;

public class AchievementHoverNode extends HoverNode {

  public final AExpression value;

  public AchievementHoverNode(
    AExpression value,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.value = value;
  }

  @Override
  public String stringify(int indentLevel) {
    return (
      indent(indentLevel) + "AchievementHoverNode{\n" +
      indent(indentLevel + 1) + "value=" + value.expressionify() + ",\n" +
      stringifyBaseMembers(indentLevel + 1) + "\n" +
      indent(indentLevel) + "}"
    );
  }
}
