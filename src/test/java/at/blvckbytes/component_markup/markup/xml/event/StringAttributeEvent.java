package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class StringAttributeEvent extends XmlEvent {

  public final String name;
  public final CursorPosition valueBeginPosition;
  public final String value;

  public StringAttributeEvent(String name, CursorPosition valueBeginPosition, String value) {
    this.name = name;
    this.valueBeginPosition = valueBeginPosition;
    this.value = value;
  }
}
