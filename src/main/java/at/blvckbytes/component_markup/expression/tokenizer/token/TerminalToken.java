package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public abstract class TerminalToken extends Token {

  protected TerminalToken(int beginIndex) {
    super(beginIndex);
  }

  public abstract @Nullable Object getPlainValue();
}
