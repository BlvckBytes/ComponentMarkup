package at.blvckbytes.component_markup.expression.tokenizer.token;

public class IdentifierToken extends TerminalToken {

  public final String identifier;

  public IdentifierToken(int charIndex, String identifier) {
    super(charIndex);

    this.identifier = identifier;
  }
}
