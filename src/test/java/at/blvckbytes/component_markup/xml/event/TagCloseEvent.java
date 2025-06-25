package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class TagCloseEvent extends Jsonifiable implements XmlEvent {

  public final String tagName;

  public TagCloseEvent(String tagName) {
    this.tagName = tagName;
  }
}
