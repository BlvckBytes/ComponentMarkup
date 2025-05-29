package at.blvckbytes.component_markup.xml.event;

public class TagCloseEvent implements XmlEvent {

  public final String tagName;

  public TagCloseEvent(String tagName) {
    this.tagName = tagName;
  }

  @Override
  public String toString() {
    return "TagCloseEvent{tagName='" + tagName + "'}";
  }
}
