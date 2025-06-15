package at.blvckbytes.component_markup.xml.event;

public class TagOpenEndEvent implements XmlEvent {

  public final String tagName;
  public final boolean wasSelfClosing;

  public TagOpenEndEvent(String tagName, boolean wasSelfClosing) {
    this.tagName = tagName;
    this.wasSelfClosing = wasSelfClosing;
  }

  @Override
  public String toString() {
    return "TagOpenEndEvent{tagName='" + tagName + "', wasSelfClosing=" + wasSelfClosing + "}";
  }
}
