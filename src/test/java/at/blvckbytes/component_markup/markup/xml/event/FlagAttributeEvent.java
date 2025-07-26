package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class FlagAttributeEvent extends XmlEvent {

  public final StringView name;

  public FlagAttributeEvent(StringView name) {
    this.name = name;
  }
}
