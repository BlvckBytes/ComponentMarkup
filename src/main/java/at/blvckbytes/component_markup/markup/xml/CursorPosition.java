package at.blvckbytes.component_markup.markup.xml;

public class CursorPosition {

  public static final CursorPosition ZERO = new CursorPosition(0, 0, 0);

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
