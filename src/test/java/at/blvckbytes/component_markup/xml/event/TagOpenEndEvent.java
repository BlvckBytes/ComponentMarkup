package at.blvckbytes.component_markup.xml.event;

public class TagOpenEndEvent implements XmlEvent {

  public final String name;
  public final boolean wasSelfClosing;

  public TagOpenEndEvent(String name, boolean wasSelfClosing) {
    this.name = name;
    this.wasSelfClosing = wasSelfClosing;
  }

  @Override
  public String toString() {
    return "TagOpenEndEvent{name='" + name + "', wasSelfClosing=" + wasSelfClosing + "}";
  }
}
