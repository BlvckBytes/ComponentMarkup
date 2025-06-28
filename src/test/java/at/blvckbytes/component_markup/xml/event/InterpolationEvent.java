package at.blvckbytes.component_markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.xml.CursorPosition;

public class InterpolationEvent extends Jsonifiable implements XmlEvent {

  public final String expression;
  public final CursorPosition valueBeginPosition;

  public InterpolationEvent(String expression, CursorPosition valueBeginPosition) {
    this.expression = expression;
    this.valueBeginPosition = valueBeginPosition;
  }
}
