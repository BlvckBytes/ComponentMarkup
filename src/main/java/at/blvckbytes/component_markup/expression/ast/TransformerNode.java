package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.StringView;

public class TransformerNode extends ExpressionNode {

  public final ExpressionNode wrapped;
  public final TransformerFunction transformer;

  public TransformerNode(ExpressionNode wrapped, TransformerFunction transformer) {
    this.wrapped = wrapped;
    this.transformer = transformer;
  }

  @Override
  public StringView getFirstMemberPositionProvider() {
    return wrapped.getFirstMemberPositionProvider();
  }

  @Override
  public StringView getLastMemberPositionProvider() {
    return wrapped.getLastMemberPositionProvider();
  }

  @Override
  public String toExpression() {
    return wrapped.toExpression();
  }
}
