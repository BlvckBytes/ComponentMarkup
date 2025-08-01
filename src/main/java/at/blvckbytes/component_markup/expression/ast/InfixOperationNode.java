package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import at.blvckbytes.component_markup.util.StringView;
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
  public StringView getFirstMemberPositionProvider() {
    return lhs.getFirstMemberPositionProvider();
  }

  @Override
  public StringView getLastMemberPositionProvider() {
    if (terminator == null)
      return rhs.getLastMemberPositionProvider();

    return terminator.raw;
  }

  @Override
  public String toExpression() {
    StringBuilder result = new StringBuilder();

    result.append(lhs.toExpression());

    boolean doSpaceOut = !(
      operatorToken.operator == InfixOperator.MEMBER
        || operatorToken.operator == InfixOperator.SUBSCRIPTING
        || operatorToken.operator == InfixOperator.RANGE
    );

    if (doSpaceOut)
      result.append(' ');

    result.append(operatorToken.operator);

    if (doSpaceOut)
      result.append(' ');

    result.append(rhs.toExpression());

    if (terminator != null)
      result.append(terminator.punctuation);

    return parenthesise(result.toString());
  }
}
