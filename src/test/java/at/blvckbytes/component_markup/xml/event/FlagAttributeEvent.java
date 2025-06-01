package at.blvckbytes.component_markup.xml.event;

public class FlagAttributeEvent implements XmlEvent {

  public final String name;

  public FlagAttributeEvent(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "FlagAttributeEvent{name='" + name + "'}";
  }
}
