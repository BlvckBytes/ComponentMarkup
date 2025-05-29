package at.blvckbytes.component_markup.xml.event;

public class TagOpenBeginEvent implements XmlEvent {

  public final String tagName;

  public TagOpenBeginEvent(String tagName) {
    this.tagName = tagName;
  }

  @Override
  public String toString() {
    return "TagOpenBeginEvent{tagName='" + tagName + "'}";
  }
}
