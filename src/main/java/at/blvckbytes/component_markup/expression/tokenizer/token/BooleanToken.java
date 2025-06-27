package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class BooleanToken extends TerminalToken {

  public final boolean value;

  public BooleanToken(int beginIndex, String raw, boolean value) {
    super(beginIndex, raw);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
