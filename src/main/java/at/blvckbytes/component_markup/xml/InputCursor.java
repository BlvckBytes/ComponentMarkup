package at.blvckbytes.component_markup.xml;

public class InputCursor {

  private final String input;
  private final XmlEventConsumer consumer;

  private final long[] stateStack;
  private int nextStateStackIndex;

  private int nextCharIndex, lineNumber, columnNumber;

  public InputCursor(String input, XmlEventConsumer consumer) {
    this.input = input;
    this.consumer = consumer;
    this.stateStack = new long[32];
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

  public void pushState() {
    if (nextStateStackIndex == stateStack.length)
      throw new IllegalStateException("Exhausted capacity of the state-stack");

    stateStack[nextStateStackIndex++] = (
      (((long) nextCharIndex & 0xFFFFFFF) << (28 + 8)) |
      (((long) lineNumber & 0xFF) << 28) |
      ((columnNumber & 0xFFFFFFF))
    );
  }

  public void popState(boolean restore, boolean emit) {
    if (nextStateStackIndex == 0)
      throw new IllegalStateException("Cannot pop off on an empty stack");

    long stateValue = stateStack[--nextStateStackIndex];

    int _nextCharIndex = (int) ((stateValue >> (28 + 8)) & 0xFFFFFFF);
    int _lineNumber = (int) ((stateValue >> 28) & 0xFF);
    int _columnNumber = (int) (stateValue & 0xFFFFFFF);

    if (restore) {
      this.nextCharIndex = _nextCharIndex;
      this.lineNumber = _lineNumber;
      this.columnNumber = _columnNumber;
    }

    if (emit)
      this.consumer.onBeforeEventCursor(_nextCharIndex == 0 ? 0 : _nextCharIndex - 1, _lineNumber, _columnNumber);
  }

  public void emitState() {
    this.consumer.onBeforeEventCursor(nextCharIndex == 0 ? 0 : nextCharIndex - 1, lineNumber, columnNumber);
  }
}
