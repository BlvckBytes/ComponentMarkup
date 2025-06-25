package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class BooleanAttributeEvent extends Jsonifiable implements XmlEvent {

  public final String name;
  public final boolean value;

  public BooleanAttributeEvent(String name, boolean value) {
    this.name = name;
    this.value = value;
  }
}
