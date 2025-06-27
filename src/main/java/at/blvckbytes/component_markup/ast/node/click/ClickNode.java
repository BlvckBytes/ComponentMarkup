package at.blvckbytes.component_markup.ast.node.click;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.built_in.click.ClickAction;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.xml.CursorPosition;

import java.util.List;

public class ClickNode extends AstNode {

  public final ClickAction action;
  public final ExpressionNode value;

  public ClickNode(
    ClickAction action,
    ExpressionNode value,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.action = action;
    this.value = value;
  }
}
