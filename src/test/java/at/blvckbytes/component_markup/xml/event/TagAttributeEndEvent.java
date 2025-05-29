package at.blvckbytes.component_markup.xml.event;

public class TagAttributeEndEvent implements XmlEvent {

  public final String name;

  public TagAttributeEndEvent(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "TagAttributeEndEvent{name='" + name + "'}";
  }
}
