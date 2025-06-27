package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import org.jetbrains.annotations.Nullable;

public class InfixOperationNode extends ExpressionNode {

  public ExpressionNode lhs;
  public InfixOperatorToken operatorToken;
  public ExpressionNode rhs;
  public @Nullable PunctuationToken terminator;

  public InfixOperationNode(
    ExpressionNode lhs,
    InfixOperatorToken operatorToken,
    ExpressionNode rhs,
    @Nullable PunctuationToken terminator
  ) {
    this.lhs = lhs;
    this.operatorToken = operatorToken;
    this.rhs = rhs;
    this.terminator = terminator;
  }

  @Override
  public int getBeginIndex() {
    return lhs.getBeginIndex();
  }

  @Override
  public int getEndIndex() {
    if (terminator == null)
      return rhs.getEndIndex();

    return terminator.endIndex;
  }
}
