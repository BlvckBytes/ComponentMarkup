package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;

public class PunctuationToken extends Token {

  public final Punctuation punctuation;

  public PunctuationToken(StringView raw, Punctuation punctuation) {
    super(raw);

    this.punctuation = punctuation;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__PUNCTUATION__ANY;
  }
}
