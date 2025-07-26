package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class NullToken extends TerminalToken {

  public NullToken(StringView raw) {
    super(raw);
  }

  @Override
  public @Nullable Object getPlainValue() {
    return null;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__LITERAL;
  }
}
