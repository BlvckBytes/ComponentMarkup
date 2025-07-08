package at.blvckbytes.component_markup.markup.xml.event;

public class BooleanAttributeEvent extends XmlEvent {

  public final String name;
  public final String raw;
  public final boolean value;

  public BooleanAttributeEvent(String name, String raw, boolean value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }
}
