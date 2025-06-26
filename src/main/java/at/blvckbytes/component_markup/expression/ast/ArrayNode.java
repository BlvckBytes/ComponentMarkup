package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;

import java.util.List;

public class ArrayNode extends ExpressionNode {

  public final InfixOperatorToken openingBracket;
  public final List<ExpressionNode> items;
  public final PunctuationToken closingBracket;

  public ArrayNode(
    InfixOperatorToken openingBracket,
    List<ExpressionNode> items,
    PunctuationToken closingBracket
  ) {
    super(openingBracket.beginIndex, closingBracket.endIndex);

    this.openingBracket = openingBracket;
    this.items = items;
    this.closingBracket = closingBracket;
  }
}
