package at.blvckbytes.component_markup.ast.node.control;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;

public class ConditionalNode extends AstNode {

  public final AExpression condition;
  public final AstNode body;

  public ConditionalNode(
    AExpression condition,
    AstNode body,
    List<LetBinding> letBindings
  ) {
    super(body.position, null, letBindings);

    this.condition = condition;
    this.body = body;
  }
}
