package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;

public class RangeNode extends ExpressionNode {

  public final ExpressionNode lowerBound;
  public final PunctuationToken boundsSeparator;
  public final ExpressionNode upperBound;

  public RangeNode(
    ExpressionNode lowerBound,
    PunctuationToken boundsSeparator,
    ExpressionNode upperBound
  ) {
    super(lowerBound.beginIndex);

    this.lowerBound = lowerBound;
    this.boundsSeparator = boundsSeparator;
    this.upperBound = upperBound;
  }
}
