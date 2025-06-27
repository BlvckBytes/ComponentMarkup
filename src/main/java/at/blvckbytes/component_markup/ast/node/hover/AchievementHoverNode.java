package at.blvckbytes.component_markup.ast.node.hover;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class AchievementHoverNode extends HoverNode {

  public final ExpressionNode value;

  public AchievementHoverNode(
    ExpressionNode value,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.value = value;
  }
}
