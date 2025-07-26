package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class BooleanAttributeEvent extends XmlEvent {

  public final StringView name;
  public final StringView raw;
  public final boolean value;

  public BooleanAttributeEvent(StringView name, StringView raw, boolean value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }
}
