package at.blvckbytes.component_markup.ast.node.click;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.ast.tag.built_in.click.ClickAction;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;

public class ClickNode extends AstNode {

  public final ClickAction action;
  public final AExpression value;

  public ClickNode(
    ClickAction action,
    AExpression value,
    CursorPosition position,
    List<AstNode> children,
    List<LetBinding> letBindings
  ) {
    super(position, children, letBindings);

    this.action = action;
    this.value = value;
  }
}
