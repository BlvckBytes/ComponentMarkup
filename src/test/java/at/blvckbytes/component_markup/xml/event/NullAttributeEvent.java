package at.blvckbytes.component_markup.xml.event;

public class NullAttributeEvent implements XmlEvent {

  public final String name;

  public NullAttributeEvent(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "NullAttributeEvent{name='" + name + "'}";
  }
}
