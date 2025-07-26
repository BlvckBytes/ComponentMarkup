package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import at.blvckbytes.component_markup.util.StringPosition;
import org.jetbrains.annotations.Nullable;

public class InfixOperationNode extends ExpressionNode {

  public ExpressionNode lhs;
  public InfixOperator operator;
  public ExpressionNode rhs;
  public @Nullable PunctuationToken terminator;

  public InfixOperationNode(
    ExpressionNode lhs,
    InfixOperator operator,
    ExpressionNode rhs,
    @Nullable PunctuationToken terminator
  ) {
    this.lhs = lhs;
    this.operator = operator;
    this.rhs = rhs;
    this.terminator = terminator;
  }

  @Override
  public StringPosition getBegin() {
    return lhs.getBegin();
  }

  @Override
  public StringPosition getEnd() {
    if (terminator == null)
      return rhs.getEnd();

    return terminator.raw.viewEnd;
  }

  @Override
  public String toExpression() {
    StringBuilder result = new StringBuilder();

    result.append(lhs.toExpression());

    boolean doSpaceOut = !(
      operator == InfixOperator.MEMBER
        || operator == InfixOperator.SUBSCRIPTING
        || operator == InfixOperator.RANGE
    );

    if (doSpaceOut)
      result.append(' ');

    result.append(operator);

    if (doSpaceOut)
      result.append(' ');

    result.append(rhs.toExpression());

    if (terminator != null)
      result.append(terminator.punctuation);

    return parenthesise(result.toString());
  }
}
