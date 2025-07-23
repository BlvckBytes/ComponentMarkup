package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class StringToken extends TerminalToken {

  public final String value;

  public StringToken(int beginIndex, String value) {
    this(beginIndex, value, "'" + value + "'");
  }

  public StringToken(int beginIndex, String value, String raw) {
    super(beginIndex, raw);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
