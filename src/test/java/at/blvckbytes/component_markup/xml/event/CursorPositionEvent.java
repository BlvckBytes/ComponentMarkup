package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.xml.CursorPosition;

public class CursorPositionEvent extends Jsonifiable implements XmlEvent {

  public final CursorPosition position;

  public CursorPositionEvent(CursorPosition position) {
    this.position = position;
  }
}
