package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.JsonifyGetter;
import at.blvckbytes.component_markup.util.StringView;

public abstract class ExpressionNode {

  public boolean parenthesised = false;

  protected ExpressionNode() {}

  public int getStartInclusive() {
    return getFirstMemberPositionProvider().startInclusive;
  }

  public int getEndExclusive() {
    return getLastMemberPositionProvider().endExclusive;
  }

  @JsonifyGetter
  public abstract StringView getFirstMemberPositionProvider();

  @JsonifyGetter
  public abstract StringView getLastMemberPositionProvider();

  public abstract String toExpression();

  protected String parenthesise(String input) {
    if (!parenthesised)
      return input;

    return "(" + input + ")";
  }
}
