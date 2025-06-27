package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class LongToken extends TerminalToken {

  public final long value;

  public LongToken(int beginIndex, String raw, long value) {
    super(beginIndex, raw);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
