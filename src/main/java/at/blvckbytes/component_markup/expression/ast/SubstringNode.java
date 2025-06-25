package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import org.jetbrains.annotations.Nullable;

public class SubstringNode extends ExpressionNode {

  public final PunctuationToken openingBracket;
  public final @Nullable ExpressionNode lowerBound;
  public final PunctuationToken boundsSeparator;
  public final @Nullable ExpressionNode upperBound;
  public final PunctuationToken closingBracket;

  public SubstringNode(
    PunctuationToken openingBracket,
    @Nullable ExpressionNode lowerBound,
    PunctuationToken boundsSeparator,
    @Nullable ExpressionNode upperBound,
    PunctuationToken closingBracket
  ) {
    super(openingBracket.beginIndex);

    this.openingBracket = openingBracket;
    this.lowerBound = lowerBound;
    this.boundsSeparator = boundsSeparator;
    this.upperBound = upperBound;
    this.closingBracket = closingBracket;
  }
}
