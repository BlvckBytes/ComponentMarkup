package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class TagOpenEndEvent extends XmlEvent {

  public final StringView tagName;
  public final boolean wasSelfClosing;

  public TagOpenEndEvent(StringView tagName, boolean wasSelfClosing) {
    this.tagName = tagName;
    this.wasSelfClosing = wasSelfClosing;
  }
}
