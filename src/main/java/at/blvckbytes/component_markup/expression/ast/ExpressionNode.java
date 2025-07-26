package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.JsonifyGetter;
import at.blvckbytes.component_markup.util.StringPosition;

public abstract class ExpressionNode {

  public boolean parenthesised = false;

  protected ExpressionNode() {}

  @JsonifyGetter
  public abstract StringPosition getBegin();

  @JsonifyGetter
  public abstract StringPosition getEnd();

  public abstract String toExpression();

  protected String parenthesise(String input) {
    if (!parenthesised)
      return input;

    return "(" + input + ")";
  }
}
