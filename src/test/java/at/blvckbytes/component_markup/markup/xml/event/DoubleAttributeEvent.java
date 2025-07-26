package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class DoubleAttributeEvent extends XmlEvent {

  public final StringView name;
  public final StringView raw;
  public final double value;

  public DoubleAttributeEvent(StringView name, StringView raw, double value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }
}
