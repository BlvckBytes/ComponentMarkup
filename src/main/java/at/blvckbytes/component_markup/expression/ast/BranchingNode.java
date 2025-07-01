package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;

public class BranchingNode extends ExpressionNode {

  public ExpressionNode condition;
  public ExpressionNode branchTrue;
  public ExpressionNode branchFalse;

  public BranchingNode(
    ExpressionNode condition,
    ExpressionNode branchTrue,
    ExpressionNode branchFalse
  ) {
    this.condition = condition;
    this.branchTrue = branchTrue;
    this.branchFalse = branchFalse;
  }

  @Override
  public int getBeginIndex() {
    return condition.getBeginIndex();
  }

  @Override
  public int getEndIndex() {
    return branchFalse.getEndIndex();
  }

  @Override
  public String toExpression() {
    return parenthesise(
      condition.toExpression()
        + " " + InfixOperator.BRANCHING
        + " " + branchTrue.toExpression()
        + " " + Punctuation.COLON
        + " " + branchFalse.toExpression()
    );
  }
}
