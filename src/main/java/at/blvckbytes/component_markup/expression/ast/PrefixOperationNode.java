package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;

public class PrefixOperationNode extends ExpressionNode {

  public PrefixOperatorToken operatorToken;
  public ExpressionNode operand;

  public PrefixOperationNode(PrefixOperatorToken operatorToken, ExpressionNode operand) {
    this.operatorToken = operatorToken;
    this.operand = operand;
  }

  @Override
  public int getStartInclusive() {
    return operatorToken.raw.startInclusive;
  }

  @Override
  public int getEndExclusive() {
    return operand.getEndExclusive();
  }

  @Override
  public String toExpression() {
    return parenthesise(operatorToken.operator + operand.toExpression());
  }
}
