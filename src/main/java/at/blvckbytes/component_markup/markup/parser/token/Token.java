package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.StringView;

public class Token {

  public final TokenType type;
  public final StringView value;

  public final int beginIndex;
  public final int endIndex;

  public Token(TokenType type, StringView value) {
    this.type = type;
    this.value = value;

    this.beginIndex = value.viewStart.charIndex;
    this.endIndex = value.viewEnd.charIndex;
  }
}
