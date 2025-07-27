package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;
import at.blvckbytes.component_markup.util.StringPosition;

public class PrefixOperationNode extends ExpressionNode {

  public PrefixOperatorToken operatorToken;
  public ExpressionNode operand;

  public PrefixOperationNode(PrefixOperatorToken operatorToken, ExpressionNode operand) {
    this.operatorToken = operatorToken;
    this.operand = operand;
  }

  @Override
  public StringPosition getStartInclusive() {
    return operatorToken.raw.startInclusive;
  }

  @Override
  public StringPosition getEndExclusive() {
    return operand.getEndExclusive();
  }

  @Override
  public String toExpression() {
    return parenthesise(operatorToken.operator + operand.toExpression());
  }
}
