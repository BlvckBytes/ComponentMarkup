package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class LongAttributeEvent extends XmlEvent {

  public final StringView name;
  public final StringView raw;
  public final long value;

  public LongAttributeEvent(StringView name, StringView raw, long value) {
    this.name = name;
    this.raw = raw;
    this.value = value;
  }
}
