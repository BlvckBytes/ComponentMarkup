package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;

public class PrefixOperationNode extends ExpressionNode {

  public final PrefixOperatorToken operator;
  public final ExpressionNode operand;

  public PrefixOperationNode(PrefixOperatorToken operator, ExpressionNode operand) {
    super(operand.beginIndex);

    this.operator = operator;
    this.operand = operand;
  }
}
