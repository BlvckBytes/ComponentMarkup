package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class FlagAttributeEvent extends Jsonifiable implements XmlEvent {

  public final String name;

  public FlagAttributeEvent(String name) {
    this.name = name;
  }
}
