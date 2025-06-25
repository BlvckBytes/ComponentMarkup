package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class StringAttributeEvent extends Jsonifiable implements XmlEvent {

  public final String name;
  public final String value;

  public StringAttributeEvent(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
