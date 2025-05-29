package at.blvckbytes.component_markup.xml.event;

public class BooleanAttributeEvent implements XmlEvent {

  public final String name;
  public final boolean value;

  public BooleanAttributeEvent(String name, boolean value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return "BooleanAttributeEvent{name='" + name + "', value=" + value + "}";
  }
}
