package at.blvckbytes.component_markup.xml.event;

public class TagAttributeBeginEvent implements XmlEvent {

  public final String name;

  public TagAttributeBeginEvent(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "TagAttributeBeginEvent{name='" + name + "'}";
  }
}
