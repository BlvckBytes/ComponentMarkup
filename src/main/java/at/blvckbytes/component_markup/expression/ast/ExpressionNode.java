/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.InterpolationMember;
import at.blvckbytes.component_markup.util.JsonifyGetter;
import at.blvckbytes.component_markup.util.InputView;

public abstract class ExpressionNode implements InterpolationMember {

  public boolean parenthesised = false;

  protected ExpressionNode() {}

  public int getStartInclusive() {
    return getFirstMemberPositionProvider().startInclusive;
  }

  public int getEndExclusive() {
    return getLastMemberPositionProvider().endExclusive;
  }

  @JsonifyGetter
  public abstract InputView getFirstMemberPositionProvider();

  @JsonifyGetter
  public abstract InputView getLastMemberPositionProvider();

  public abstract String toExpression();

  protected String parenthesise(String input) {
    if (!parenthesised)
      return input;

    return "(" + input + ")";
  }
}
