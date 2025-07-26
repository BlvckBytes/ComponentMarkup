package at.blvckbytes.component_markup.expression.tokenizer.token;

import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class DoubleToken extends TerminalToken {

  public final double value;

  public DoubleToken(StringView raw, double value) {
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
