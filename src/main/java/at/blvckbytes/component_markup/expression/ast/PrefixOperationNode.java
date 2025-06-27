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
  public int getBeginIndex() {
    return operatorToken.beginIndex;
  }

  @Override
  public int getEndIndex() {
    return operand.getEndIndex();
  }
}
