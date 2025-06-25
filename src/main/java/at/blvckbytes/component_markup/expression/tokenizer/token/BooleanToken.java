package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class BooleanToken extends TerminalToken {

  public final boolean value;

  public BooleanToken(int charIndex, boolean value) {
    super(charIndex);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }
}
