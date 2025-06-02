package at.blvckbytes.component_markup.xml;

public class CursorPosition {

  public final int nextCharIndex, lineNumber, columnNumber;

  public CursorPosition(
    int nextCharIndex,
    int lineNumber,
    int columnNumber
  ) {
    this.nextCharIndex = nextCharIndex;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }
}
