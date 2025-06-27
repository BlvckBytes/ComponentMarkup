package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class NullToken extends TerminalToken {

  public NullToken(int beginIndex, String raw) {
    super(beginIndex, raw);
  }

  @Override
  public @Nullable Object getPlainValue() {
    return null;
  }
}
