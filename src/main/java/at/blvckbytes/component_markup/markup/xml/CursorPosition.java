package at.blvckbytes.component_markup.markup.xml;

public class CursorPosition {

  public final int nextCharIndex, lineNumber, columnNumber;
  public final String input;

  public CursorPosition(int nextCharIndex, int lineNumber, int columnNumber, String input) {
    this.nextCharIndex = nextCharIndex;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.input = input;
  }
}
