package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class LongAttributeEvent extends Jsonifiable implements XmlEvent {

  public final String name;
  public final long value;

  public LongAttributeEvent(String name, long value) {
    this.name = name;
    this.value = value;
  }
}
