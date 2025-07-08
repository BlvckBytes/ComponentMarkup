package at.blvckbytes.component_markup.markup.xml.event;

public class LongAttributeEvent extends XmlEvent {

  public final String name;
  public final String raw;
  public final long value;

  public LongAttributeEvent(String name, String raw, long value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }
}
