package at.blvckbytes.component_markup.xml;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class CursorPosition extends Jsonifiable {

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
