package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import org.jetbrains.annotations.Nullable;

public class InputCursor {

  public final String input;
  private final @Nullable TokenOutput tokenOutput;
  private int nextCharIndex, lineNumber, columnNumber;

  public InputCursor(String input, @Nullable TokenOutput tokenOutput) {
    this.input = input;
    this.tokenOutput = tokenOutput;
    this.lineNumber = 1;
  }

  public int getNextCharIndex() {
    return this.nextCharIndex;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  public char nextChar() {
    if (this.nextCharIndex == input.length())
      return 0;

    char currentChar = input.charAt(this.nextCharIndex++);

    if (currentChar == '\n') {
      ++lineNumber;
      columnNumber = 0;
      return currentChar;
    }

    ++columnNumber;
    return currentChar;
  }

  public char peekChar() {
    if (this.nextCharIndex == input.length())
      return 0;

    return input.charAt(this.nextCharIndex);
  }

  public void consumeWhitespace() {
    char c;

    while (Character.isWhitespace(c = peekChar())) {
      if (tokenOutput != null)
        tokenOutput.emitToken(nextCharIndex, TokenType.ANY__WHITESPACE, String.valueOf(c));

      nextChar();
    }
  }

  public CursorPosition getPosition() {
    return new CursorPosition(nextCharIndex, lineNumber, columnNumber, input);
  }

  public void restoreState(CursorPosition position) {
    this.nextCharIndex = position.nextCharIndex;
    this.lineNumber = position.lineNumber;
    this.columnNumber = position.columnNumber;
  }
}
