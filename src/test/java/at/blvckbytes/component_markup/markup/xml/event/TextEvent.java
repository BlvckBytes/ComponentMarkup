package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class TextEvent extends Jsonifiable implements XmlEvent {

  public final String text;

  public TextEvent(String text) {
    this.text = text;
  }
}
