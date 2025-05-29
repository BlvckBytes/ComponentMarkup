package at.blvckbytes.component_markup.xml.event;

public class LongAttributeEvent implements XmlEvent {

  public final String name;
  public final long value;

  public LongAttributeEvent(String name, long value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return "LongAttributeEvent{name='" + name + "', value=" + value + "}";
  }
}
