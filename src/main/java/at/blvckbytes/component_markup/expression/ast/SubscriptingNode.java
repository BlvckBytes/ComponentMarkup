package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;

public class SubscriptingNode extends ExpressionNode {

  public final ExpressionNode lhs;
  public final InfixOperatorToken openingBracket;
  public final ExpressionNode rhs;
  public final PunctuationToken closingBracket;

  public SubscriptingNode(
    ExpressionNode lhs,
    InfixOperatorToken openingBracket,
    ExpressionNode rhs,
    PunctuationToken closingBracket
  ) {
    super(lhs.beginIndex);

    this.lhs = lhs;
    this.openingBracket = openingBracket;
    this.rhs = rhs;
    this.closingBracket = closingBracket;
  }
}
