package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class StringToken extends TerminalToken {

  public final String value;

  public StringToken(int beginIndex, String value) {
    super(beginIndex, "'" + value + "'");

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
