package at.blvckbytes.component_markup.xml.event;

public class DoubleAttributeEvent implements XmlEvent {

  public final String name;
  public final double value;

  public DoubleAttributeEvent(String name, double value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return "DoubleAttributeEvent{name='" + name + "', value=" + value + "}";
  }
}
