/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;
import at.blvckbytes.component_markup.util.InputView;

public class PrefixOperationNode extends ExpressionNode {

  public PrefixOperatorToken operatorToken;
  public ExpressionNode operand;

  public PrefixOperationNode(PrefixOperatorToken operatorToken, ExpressionNode operand) {
    this.operatorToken = operatorToken;
    this.operand = operand;
  }

  @Override
  public InputView getFirstMemberPositionProvider() {
    return operatorToken.raw;
  }

  @Override
  public InputView getLastMemberPositionProvider() {
    return operand.getLastMemberPositionProvider();
  }

  @Override
  public String toExpression() {
    return parenthesise(operatorToken.operator + operand.toExpression());
  }
}
