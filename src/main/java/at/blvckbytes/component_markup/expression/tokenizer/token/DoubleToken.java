package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class DoubleToken extends TerminalToken {

  public final double value;

  public DoubleToken(int beginIndex, double value) {
    super(beginIndex);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
