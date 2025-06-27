package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.util.JsonifyGetter;

public abstract class ExpressionNode extends Jsonifiable {

  public boolean parenthesised = false;

  protected ExpressionNode() {}

  @JsonifyGetter
  public abstract int getBeginIndex();

  @JsonifyGetter
  public abstract int getEndIndex();

  public abstract String toExpression();

  protected String parenthesise(String input) {
    if (!parenthesised)
      return input;

    return "(" + input + ")";
  }
}
