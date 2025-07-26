package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class IdentifierToken extends TerminalToken {

  public final String identifier;

  public IdentifierToken(StringView identifier) {
    super(identifier);

    this.identifier = identifier.buildString();
  }

  @Override
  public @Nullable Object getPlainValue() {
    return identifier;
  }

  @Override
  public TokenType getType() {
    return TokenType.EXPRESSION__IDENTIFIER_ANY;
  }
}
