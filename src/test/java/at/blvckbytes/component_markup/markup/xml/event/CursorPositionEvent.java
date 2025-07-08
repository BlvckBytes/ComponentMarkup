package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class CursorPositionEvent extends XmlEvent {

  public final CursorPosition position;

  public CursorPositionEvent(CursorPosition position) {
    this.position = position;
  }
}
