package at.blvckbytes.component_markup.expression.tokenizer.token;

public class DoubleToken extends TerminalToken {

  public final double value;

  public DoubleToken(int charIndex, double value) {
    super(charIndex);

    this.value = value;
  }
}
