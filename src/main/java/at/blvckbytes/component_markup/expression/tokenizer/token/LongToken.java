package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class LongToken extends TerminalToken {

  public final long value;

  public LongToken(StringView raw, long value) {
    super(raw);

    this.value = value;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return value;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__NUMBER;
  }
}
