package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class RawNode extends ExpressionNode {

  public final @Nullable Object value;

  public RawNode(@Nullable Object value) {
    this.value = value;
  }

  @Override
  public InputView getFirstMemberPositionProvider() {
    return InputView.EMPTY;
  }

  @Override
  public InputView getLastMemberPositionProvider() {
    return InputView.EMPTY;
  }

  @Override
  public String toExpression() {
    return "<" + getClass().getName() + ">";
  }
}
