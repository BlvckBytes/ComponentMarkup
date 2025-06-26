package at.blvckbytes.component_markup.expression.tokenizer.token;

import org.jetbrains.annotations.Nullable;

public class IdentifierToken extends TerminalToken {

  public final String identifier;

  public IdentifierToken(int beginIndex, String identifier) {
    super(beginIndex, beginIndex + (identifier.length() - 1));

    this.identifier = identifier;
  }

  @Override
  public @Nullable Object getPlainValue() {
    return identifier;
  }
}
