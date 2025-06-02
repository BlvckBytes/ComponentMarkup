package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;
import java.util.Map;

public class ConditionalNode extends ContentNode {

  public final AExpression conditionExpression;
  public final AstNode body;

  public ConditionalNode(
    AExpression conditionExpression,
    AstNode body,
    List<AstNode> children,
    Map<String, AExpression> letBindings
  ) {
    super(children, letBindings);

    this.conditionExpression = conditionExpression;
    this.body = body;
  }
}
