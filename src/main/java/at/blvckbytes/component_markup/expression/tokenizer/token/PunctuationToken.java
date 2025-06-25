package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;

public class PunctuationToken extends ExpressionToken {

  public final Punctuation punctuation;

  public PunctuationToken(int charIndex, Punctuation punctuation) {
    super(charIndex);

    this.punctuation = punctuation;
  }
}
