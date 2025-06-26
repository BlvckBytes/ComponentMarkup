package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class NullToken extends TerminalToken {

  public NullToken(int beginIndex) {
    super(beginIndex, beginIndex + (4 - 1));
  }

  @Override
  public @Nullable Object getPlainValue() {
    return null;
  }
}
