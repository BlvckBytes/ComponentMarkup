package at.blvckbytes.component_markup.expression.tokenizer.token;

public class BooleanToken extends TerminalToken {

  public final boolean value;

  public BooleanToken(int charIndex, boolean value) {
    super(charIndex);

    this.value = value;
  }
}
