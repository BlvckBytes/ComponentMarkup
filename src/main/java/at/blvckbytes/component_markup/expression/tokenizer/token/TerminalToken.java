package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public abstract class TerminalToken extends Token {

  public final String raw;

  protected TerminalToken(int beginIndex, String raw) {
    super(beginIndex, beginIndex + (raw.length() - 1));

    this.raw = raw;
  }

  public abstract @Nullable Object getPlainValue();
}
