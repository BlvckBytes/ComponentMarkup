package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class TagOpenEndEvent extends Jsonifiable implements XmlEvent {

  public final String tagName;
  public final boolean wasSelfClosing;

  public TagOpenEndEvent(String tagName, boolean wasSelfClosing) {
    this.tagName = tagName;
    this.wasSelfClosing = wasSelfClosing;
  }
}
