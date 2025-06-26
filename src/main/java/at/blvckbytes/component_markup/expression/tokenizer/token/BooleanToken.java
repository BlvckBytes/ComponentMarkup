package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class BooleanToken extends TerminalToken {

  public final boolean value;

  public BooleanToken(int beginIndex, boolean value) {
    super(beginIndex, beginIndex + ((value ? 4 : 5) - 1));

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
