package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;

public class InterpolationEvent extends Jsonifiable implements XmlEvent {

  public final String expression;

  public InterpolationEvent(String expression) {
    this.expression = expression;
  }
}
