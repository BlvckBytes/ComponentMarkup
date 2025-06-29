package at.blvckbytes.component_markup.expression.ast;

public class TransformerNode extends ExpressionNode {

  public final ExpressionNode wrapped;
  public final TransformerFunction transformer;

  public TransformerNode(ExpressionNode wrapped, TransformerFunction transformer) {
    this.wrapped = wrapped;
    this.transformer = transformer;
  }

  @Override
  public int getBeginIndex() {
    return wrapped.getBeginIndex();
  }

  @Override
  public int getEndIndex() {
    return wrapped.getEndIndex();
  }

  @Override
  public String toExpression() {
    return wrapped.toExpression();
  }
}
