package at.blvckbytes.component_markup.expression.tokenizer.token;

public class LongToken extends ExpressionToken {

  public final long value;

  public LongToken(int charIndex, long value) {
    super(charIndex);

    this.value = value;
  }
}
