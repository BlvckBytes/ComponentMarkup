package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import org.jetbrains.annotations.Nullable;

public class BranchingNode extends ExpressionNode {

  public ExpressionNode condition;
  public @Nullable InfixOperatorToken branchingOperator;
  public ExpressionNode branchTrue;
  public @Nullable PunctuationToken branchingSeparator;
  public ExpressionNode branchFalse;

  public BranchingNode(
    ExpressionNode condition,
    @Nullable InfixOperatorToken branchingOperator,
    ExpressionNode branchTrue,
    @Nullable PunctuationToken branchingSeparator,
    ExpressionNode branchFalse
  ) {
    this.condition = condition;
    this.branchingOperator = branchingOperator;
    this.branchTrue = branchTrue;
    this.branchingSeparator = branchingSeparator;
    this.branchFalse = branchFalse;
  }

  @Override
  public int getStartInclusive() {
    return condition.getStartInclusive();
  }

  @Override
  public int getEndExclusive() {
    return branchFalse.getEndExclusive();
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
