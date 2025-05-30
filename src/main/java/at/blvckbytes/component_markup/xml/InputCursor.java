package at.blvckbytes.component_markup.xml;

public class InputCursor {

  private final String input;
  private final XmlEventConsumer consumer;

  private int nextCharIndex, lineNumber, columnNumber;

  public InputCursor(String input, XmlEventConsumer consumer) {
    this.input = input;
    this.consumer = consumer;
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

  public long getState() {
    return (
      (((long) nextCharIndex & 0xFFFFFFF) << (28 + 8)) |
      (((long) lineNumber & 0xFF) << 28) |
      ((columnNumber & 0xFFFFFFF))
    );
  }

  public void emitState(long state) {
    int _nextCharIndex = (int) ((state >> (28 + 8)) & 0xFFFFFFF);
    int _lineNumber = (int) ((state >> 28) & 0xFF);
    int _columnNumber = (int) (state & 0xFFFFFFF);

    this.consumer.onBeforeEventCursor(_nextCharIndex == 0 ? 0 : _nextCharIndex - 1, _lineNumber, _columnNumber);
  }

  public void restoreState(long state) {
    this.nextCharIndex = (int) ((state >> (28 + 8)) & 0xFFFFFFF);
    this.lineNumber = (int) ((state >> 28) & 0xFF);
    this.columnNumber = (int) (state & 0xFFFFFFF);
  }

  public void emitCurrentState() {
    this.consumer.onBeforeEventCursor(nextCharIndex == 0 ? 0 : nextCharIndex - 1, lineNumber, columnNumber);
  }
}
