package at.blvckbytes.component_markup.ast.node;

import at.blvckbytes.component_markup.ast.node.content.ContentNode;
import me.blvckbytes.gpeee.parser.expression.AExpression;

import java.util.List;
import java.util.Map;

public class ForLoopNode extends ContentNode {

  public final AExpression iterableExpression;
  public final String iterationVariableName;
  public final AstNode body;

  public ForLoopNode(
    AExpression iterableExpression,
    String iterationVariableName,
    AstNode body,
    List<AstNode> children,
    Map<String, AExpression> letBindings
  ) {
    super(children, letBindings);

    this.body = body;
    this.iterableExpression = iterableExpression;
    this.iterationVariableName = iterationVariableName;
  }
}
