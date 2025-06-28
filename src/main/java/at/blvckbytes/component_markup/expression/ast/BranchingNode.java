package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;

public class BranchingNode extends ExpressionNode {

  public ExpressionNode condition;
  public InfixOperatorToken conditionSeparator;
  public ExpressionNode branchTrue;
  public PunctuationToken branchSeparator;
  public ExpressionNode branchFalse;

  public BranchingNode(
    ExpressionNode condition,
    InfixOperatorToken conditionSeparator,
    ExpressionNode branchTrue,
    PunctuationToken branchSeparator,
    ExpressionNode branchFalse
  ) {
    this.condition = condition;
    this.conditionSeparator = conditionSeparator;
    this.branchTrue = branchTrue;
    this.branchSeparator = branchSeparator;
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
        + " " + conditionSeparator.operator
        + " " + branchTrue.toExpression()
        + " " + branchSeparator.punctuation
        + " " + branchFalse.toExpression()
    );
  }
}
