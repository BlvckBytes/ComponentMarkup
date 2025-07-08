package at.blvckbytes.component_markup.markup.xml.event;

public class TagOpenBeginEvent extends XmlEvent {

  public final String tagName;

  public TagOpenBeginEvent(String tagName) {
    this.tagName = tagName;
  }
}
