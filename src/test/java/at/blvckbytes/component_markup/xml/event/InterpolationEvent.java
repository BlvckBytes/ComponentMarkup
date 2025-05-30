package at.blvckbytes.component_markup.xml.event;

public class InterpolationEvent implements XmlEvent {

  public final String expression;

  public InterpolationEvent(String expression) {
    this.expression = expression;
  }

  @Override
  public String toString() {
    return "InterpolationEvent{expression='" + expression + "'}";
  }
}
