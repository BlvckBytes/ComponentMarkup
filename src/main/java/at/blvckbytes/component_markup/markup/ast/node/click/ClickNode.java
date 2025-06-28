package at.blvckbytes.component_markup.markup.ast.node.click;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.click.ClickAction;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;

import java.util.List;

public class ClickNode extends MarkupNode {

  public final ClickAction action;
  public final ExpressionNode value;

  public ClickNode(
    ClickAction action,
    ExpressionNode value,
    CursorPosition position,
    List<MarkupNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.action = action;
    this.value = value;
  }
}
