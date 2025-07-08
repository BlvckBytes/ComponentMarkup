package at.blvckbytes.component_markup.markup.xml.event;

public class DoubleAttributeEvent extends XmlEvent {

  public final String name;
  public final String raw;
  public final double value;

  public DoubleAttributeEvent(String name, String raw, double value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }
}
