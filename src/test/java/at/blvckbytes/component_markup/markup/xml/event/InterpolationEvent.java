package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.util.StringView;

public class InterpolationEvent extends XmlEvent {

  public final StringView expression;

  public InterpolationEvent(StringView expression) {
    this.expression = expression;
  }
}
