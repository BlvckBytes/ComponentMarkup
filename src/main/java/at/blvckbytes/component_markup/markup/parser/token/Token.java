package at.blvckbytes.component_markup.markup.parser.token;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Token {

  public final TokenType type;
  public final int beginIndex;
  public final String value;

  private @Nullable List<Token> children;

  public Token(TokenType type, int beginIndex, char value) {
    this(type, beginIndex, String.valueOf(value));
  }

  public Token(TokenType type, int beginIndex, String value) {
    this.type = type;
    this.beginIndex = beginIndex;
    this.value = value;
  }

  public void addChild(Token token) {
    if (children == null)
      children = new ArrayList<>();

    children.add(token);
  }

  public @Nullable List<Token> getChildren() {
    return children;
  }
}
