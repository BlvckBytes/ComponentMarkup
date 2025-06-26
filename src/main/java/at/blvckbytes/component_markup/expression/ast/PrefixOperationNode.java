package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;

public class PrefixOperationNode extends ExpressionNode {

  public final PrefixOperatorToken operatorToken;
  public final ExpressionNode operand;

  public PrefixOperationNode(PrefixOperatorToken operatorToken, ExpressionNode operand) {
    super(operand.beginIndex, operand.endIndex);

    this.operatorToken = operatorToken;
    this.operand = operand;
  }
}
