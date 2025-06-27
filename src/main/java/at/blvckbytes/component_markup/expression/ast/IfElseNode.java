package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;

public class IfElseNode extends ExpressionNode {

  public final ExpressionNode condition;
  public final InfixOperatorToken conditionSeparator;
  public final ExpressionNode branchTrue;
  public final PunctuationToken branchSeparator;
  public final ExpressionNode branchFalse;

  public IfElseNode(
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
}
