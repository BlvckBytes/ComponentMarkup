package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.xml.CursorPosition;

public class CursorPositionEvent implements XmlEvent {

  public final CursorPosition position;

  public CursorPositionEvent(CursorPosition position) {
    this.position = position;
  }

  @Override
  public String toString() {
    return "CursorPositionEvent{nextCharIndex=" + position.nextCharIndex + ", lineNumber=" + position.lineNumber + ", columnNumber=" + position.columnNumber + "}";
  }
}
