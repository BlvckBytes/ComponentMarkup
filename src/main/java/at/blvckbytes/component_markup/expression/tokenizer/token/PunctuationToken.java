package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;

public class PunctuationToken extends Token {

  public final Punctuation punctuation;

  public PunctuationToken(int beginIndex, Punctuation punctuation) {
    super(beginIndex, beginIndex + (punctuation.length - 1));

    this.punctuation = punctuation;
  }
}
