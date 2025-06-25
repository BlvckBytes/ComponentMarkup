package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class TagAttributeBeginEvent extends Jsonifiable implements XmlEvent {

  public final String name;

  public TagAttributeBeginEvent(String name) {
    this.name = name;
  }
}
