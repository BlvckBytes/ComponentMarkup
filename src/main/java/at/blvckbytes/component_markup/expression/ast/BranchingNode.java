/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.PunctuationToken;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class BranchingNode extends ExpressionNode {

  public ExpressionNode condition;
  public @Nullable InfixOperatorToken branchingOperator;
  public ExpressionNode branchTrue;
  public @Nullable PunctuationToken branchingSeparator;
  public ExpressionNode branchFalse;

  public BranchingNode(
    ExpressionNode condition,
    @Nullable InfixOperatorToken branchingOperator,
    ExpressionNode branchTrue,
    @Nullable PunctuationToken branchingSeparator,
    ExpressionNode branchFalse
  ) {
    this.condition = condition;
    this.branchingOperator = branchingOperator;
    this.branchTrue = branchTrue;
    this.branchingSeparator = branchingSeparator;
    this.branchFalse = branchFalse;
  }

  @Override
  public StringView getFirstMemberPositionProvider() {
    return condition.getFirstMemberPositionProvider();
  }

  @Override
  public StringView getLastMemberPositionProvider() {
    return branchFalse.getLastMemberPositionProvider();
  }

  @Override
  public String toExpression() {
    return parenthesise(
      condition.toExpression()
        + " " + InfixOperator.BRANCHING
        + " " + branchTrue.toExpression()
        + " " + Punctuation.COLON
        + " " + branchFalse.toExpression()
    );
  }
}
