package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class TagAttributeEndEvent extends Jsonifiable implements XmlEvent {

  public final String name;

  public TagAttributeEndEvent(String name) {
    this.name = name;
  }
}
