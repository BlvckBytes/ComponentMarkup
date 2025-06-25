package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;

public class InfixOperationNode extends ExpressionNode {

  public final ExpressionNode lhs;
  public final InfixOperatorToken operatorToken;
  public final ExpressionNode rhs;

  public InfixOperationNode(ExpressionNode lhs, InfixOperatorToken operatorToken, ExpressionNode rhs) {
    super(lhs.beginIndex);

    this.lhs = lhs;
    this.operatorToken = operatorToken;
    this.rhs = rhs;
  }
}
