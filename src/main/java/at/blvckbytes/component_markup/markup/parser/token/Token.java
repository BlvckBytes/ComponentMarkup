package at.blvckbytes.component_markup.markup.parser.token;

public class Token {

  public final TokenType type;
  public final int beginIndex;
  public final int endIndex;
  public final String value;

  public Token(TokenType type, int beginIndex, char value) {
    this(type, beginIndex, String.valueOf(value));
  }

  public Token(TokenType type, int beginIndex, String value) {
    if (value == null || value.isEmpty())
      throw new IllegalStateException("Illegal " + (value == null ? "null" : "empty") + " token " + type + " at index " + beginIndex);

    this.type = type;
    this.beginIndex = beginIndex;
    this.endIndex = beginIndex + (value.length() - 1);
    this.value = value;
  }
}
