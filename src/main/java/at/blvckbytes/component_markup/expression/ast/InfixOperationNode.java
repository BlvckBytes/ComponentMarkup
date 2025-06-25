package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;

public class InfixOperationNode extends ExpressionNode {

  public final ExpressionNode lhs;
  public final InfixOperatorToken operator;
  public final ExpressionNode rhs;

  public InfixOperationNode(ExpressionNode lhs, InfixOperatorToken operator, ExpressionNode rhs) {
    super(lhs.beginIndex);

    this.lhs = lhs;
    this.operator = operator;
    this.rhs = rhs;
  }
}
