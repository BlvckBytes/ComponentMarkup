package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;

public class ConditionalNode extends ContentNode {

  public final AExpression conditionExpression;
  public final AstNode body;

  public ConditionalNode(
    AExpression conditionExpression,
    AstNode body,
    List<LetBinding> letBindings
  ) {
    super(null, letBindings);

    this.conditionExpression = conditionExpression;
    this.body = body;
  }
}
