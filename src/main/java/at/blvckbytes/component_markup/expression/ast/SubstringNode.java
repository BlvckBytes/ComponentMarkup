package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import org.jetbrains.annotations.Nullable;

public class SubstringNode extends ExpressionNode {

  public ExpressionNode operand;
  public InfixOperatorToken openingBracket;
  public @Nullable ExpressionNode lowerBound;
  public PunctuationToken boundsSeparator;
  public @Nullable ExpressionNode upperBound;
  public PunctuationToken closingBracket;

  public SubstringNode(
    ExpressionNode operand,
    InfixOperatorToken openingBracket,
    @Nullable ExpressionNode lowerBound,
    PunctuationToken boundsSeparator,
    @Nullable ExpressionNode upperBound,
    PunctuationToken closingBracket
  ) {
    this.operand = operand;
    this.openingBracket = openingBracket;
    this.lowerBound = lowerBound;
    this.boundsSeparator = boundsSeparator;
    this.upperBound = upperBound;
    this.closingBracket = closingBracket;
  }

  @Override
  public int getStartInclusive() {
    return operand.getStartInclusive();
  }

  @Override
  public int getEndExclusive() {
    return closingBracket.raw.endExclusive;
  }

  @Override
  public String toExpression() {
    return parenthesise(
      operand.toExpression()
        + openingBracket.operator
        + (lowerBound == null ? "" : lowerBound.toExpression())
        + ":"
        + (upperBound == null ? "" : upperBound.toExpression())
        + closingBracket.punctuation
    );
  }
}
