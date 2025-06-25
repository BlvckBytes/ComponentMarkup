package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class DoubleAttributeEvent extends Jsonifiable implements XmlEvent {

  public final String name;
  public final double value;

  public DoubleAttributeEvent(String name, double value) {
    this.name = name;
    this.value = value;
  }
}
