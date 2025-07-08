package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;

public class InterpolationEvent extends XmlEvent {

  public final String expression;
  public final CursorPosition valueBeginPosition;

  public InterpolationEvent(String expression, CursorPosition valueBeginPosition) {
    this.expression = expression;
    this.valueBeginPosition = valueBeginPosition;
  }
}
