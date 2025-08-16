/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.InputView;

import java.util.Map;

public class MapNode extends ExpressionNode {

  public Token openingBracket;
  public MapNodeItems items;
  public Token closingBracket;

  public MapNode(Token openingBracket, MapNodeItems items, Token closingBracket) {
    this.openingBracket = openingBracket;
    this.items = items;
    this.closingBracket = closingBracket;
  }

  @Override
  public InputView getFirstMemberPositionProvider() {
    return openingBracket.raw;
  }

  @Override
  public InputView getLastMemberPositionProvider() {
    return closingBracket.raw;
  }

  @Override
  public String toExpression() {
    StringBuilder result = new StringBuilder();

    result.append('{');

    for (Map.Entry<String, ExpressionNode> entry : items.entrySet()) {
      if (result.length() != 1)
        result.append(", ");

      result.append(entry.getKey()).append(": ").append(entry.getValue().toExpression());
    }

    result.append('}');

    return parenthesise(result.toString());
  }
}
