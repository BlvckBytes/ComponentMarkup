package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.StringPosition;

public class TransformerNode extends ExpressionNode {

  public final ExpressionNode wrapped;
  public final TransformerFunction transformer;

  public TransformerNode(ExpressionNode wrapped, TransformerFunction transformer) {
    this.wrapped = wrapped;
    this.transformer = transformer;
  }

  @Override
  public StringPosition getBegin() {
    return wrapped.getBegin();
  }

  @Override
  public StringPosition getEnd() {
    return wrapped.getEnd();
  }

  @Override
  public String toExpression() {
    return wrapped.toExpression();
  }
}
