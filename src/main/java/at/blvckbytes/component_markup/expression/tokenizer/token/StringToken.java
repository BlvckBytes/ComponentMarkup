package at.blvckbytes.component_markup.expression.tokenizer.token;

public class StringToken extends ExpressionToken {

  public final String value;

  public StringToken(int charIndex, String value) {
    super(charIndex);

    this.value = value;
  }
}
