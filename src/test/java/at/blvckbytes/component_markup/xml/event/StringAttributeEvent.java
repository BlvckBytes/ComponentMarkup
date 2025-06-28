package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.xml.CursorPosition;

public class StringAttributeEvent extends Jsonifiable implements XmlEvent {

  public final String name;
  public final CursorPosition valueBeginPosition;
  public final String value;

  public StringAttributeEvent(String name, CursorPosition valueBeginPosition, String value) {
    this.name = name;
    this.valueBeginPosition = valueBeginPosition;
    this.value = value;
  }
}
