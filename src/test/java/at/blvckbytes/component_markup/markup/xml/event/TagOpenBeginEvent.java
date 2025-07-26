package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class TagOpenBeginEvent extends XmlEvent {

  public final StringView tagName;

  public TagOpenBeginEvent(StringView tagName) {
    this.tagName = tagName;
  }
}
