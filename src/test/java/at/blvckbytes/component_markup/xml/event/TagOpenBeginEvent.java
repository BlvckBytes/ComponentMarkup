package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class TagOpenBeginEvent extends Jsonifiable implements XmlEvent {

  public final String tagName;

  public TagOpenBeginEvent(String tagName) {
    this.tagName = tagName;
  }
}
