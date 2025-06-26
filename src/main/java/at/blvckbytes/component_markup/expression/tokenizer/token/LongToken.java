package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class LongToken extends TerminalToken {

  public final long value;

  public LongToken(int beginIndex, long value) {
    super(beginIndex);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
