package at.blvckbytes.component_markup.expression.tokenizer.token;

public class BooleanToken extends ExpressionToken {

  public final boolean value;

  public BooleanToken(int charIndex, boolean value) {
    super(charIndex);

    this.value = value;
  }
}
