package at.blvckbytes.component_markup.xml.event;

public class StringAttributeEvent implements XmlEvent {

  public final String name;
  public final String value;

  public StringAttributeEvent(String name, String value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return "StringAttributeEvent{name='" + name + "', value='" + value + "'}";
  }
}
