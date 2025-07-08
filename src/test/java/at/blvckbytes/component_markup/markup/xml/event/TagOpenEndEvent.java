package at.blvckbytes.component_markup.markup.xml.event;

public class TagOpenEndEvent extends XmlEvent {

  public final String tagName;
  public final boolean wasSelfClosing;

  public TagOpenEndEvent(String tagName, boolean wasSelfClosing) {
    this.tagName = tagName;
    this.wasSelfClosing = wasSelfClosing;
  }
}
