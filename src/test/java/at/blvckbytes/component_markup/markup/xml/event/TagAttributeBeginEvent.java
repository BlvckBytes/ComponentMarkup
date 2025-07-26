package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class TagAttributeBeginEvent extends XmlEvent {

  public final StringView name;

  public TagAttributeBeginEvent(StringView name) {
    this.name = name;
  }
}
