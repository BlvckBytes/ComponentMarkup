package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class BooleanAttributeEvent extends Jsonifiable implements XmlEvent {

  public final String name;
  public final String raw;
  public final boolean value;

  public BooleanAttributeEvent(String name, String raw, boolean value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }
}
