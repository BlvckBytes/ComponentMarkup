package at.blvckbytes.component_markup.xml;

public class InputCursor {

  private final String input;

  private int nextCharIndex, lineNumber, columnNumber;

  public InputCursor(String input) {
    this.input = input;
    this.lineNumber = 1;
  }

  public int getNextCharIndex() {
    return this.nextCharIndex;
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

  public boolean hasRemainingChars() {
    return nextCharIndex < input.length();
  }

  public void consumeWhitespace() {
    while (Character.isWhitespace(peekChar()))
      nextChar();
  }

  public CursorPosition getPosition() {
    return new CursorPosition(nextCharIndex, lineNumber, columnNumber);
  }

  public void restoreState(CursorPosition position) {
    this.nextCharIndex = position.nextCharIndex;
    this.lineNumber = position.lineNumber;
    this.columnNumber = position.columnNumber;
  }
}
