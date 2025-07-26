package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringPosition;

public class PositionEvent extends XmlEvent {

  public final StringPosition position;

  public PositionEvent(StringPosition position) {
    this.position = position;
  }
}
