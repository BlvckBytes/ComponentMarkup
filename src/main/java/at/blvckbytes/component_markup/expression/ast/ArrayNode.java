/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import at.blvckbytes.component_markup.util.StringView;

import java.util.List;

public class ArrayNode extends ExpressionNode {

  public InfixOperatorToken openingBracket;
  public List<ExpressionNode> items;
  public PunctuationToken closingBracket;

  public ArrayNode(
    InfixOperatorToken openingBracket,
    List<ExpressionNode> items,
    PunctuationToken closingBracket
  ) {
    this.openingBracket = openingBracket;
    this.items = items;
    this.closingBracket = closingBracket;
  }

  @Override
  public StringView getFirstMemberPositionProvider() {
    return openingBracket.raw;
  }

  @Override
  public StringView getLastMemberPositionProvider() {
    return closingBracket.raw;
  }

  @Override
  public String toExpression() {
    StringBuilder result = new StringBuilder();

    result.append(openingBracket.operator);

    for (int itemIndex = 0; itemIndex < items.size(); ++itemIndex) {
      if (itemIndex != 0)
        result.append(", ");

      result.append(items.get(itemIndex).toExpression());
    }

    result.append(closingBracket.punctuation);

    return parenthesise(result.toString());
  }
}
