package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.JsonifyGetter;

public abstract class ExpressionNode {

  public boolean parenthesised = false;

  protected ExpressionNode() {}

  @JsonifyGetter
  public abstract int getStartInclusive();

  @JsonifyGetter
  public abstract int getEndExclusive();

  public abstract String toExpression();

  protected String parenthesise(String input) {
    if (!parenthesised)
      return input;

    return "(" + input + ")";
  }
}
