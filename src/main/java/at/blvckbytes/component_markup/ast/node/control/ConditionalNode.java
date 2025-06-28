package at.blvckbytes.component_markup.ast.node.control;

import at.blvckbytes.component_markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;

import java.util.List;

public class ConditionalNode extends MarkupNode {

  public final ExpressionNode condition;
  public final MarkupNode body;

  public ConditionalNode(
    ExpressionNode condition,
    MarkupNode body,
    List<LetBinding> letBindings
  ) {
    super(body.position, null, letBindings);

    this.condition = condition;
    this.body = body;
  }
}
