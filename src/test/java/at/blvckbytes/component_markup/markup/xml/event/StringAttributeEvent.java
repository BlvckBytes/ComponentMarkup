package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class StringAttributeEvent extends XmlEvent {

  public final StringView name;
  public final StringView value;

  public StringAttributeEvent(StringView name, StringView value) {
    this.name = name;
    this.value = value;
  }
}
