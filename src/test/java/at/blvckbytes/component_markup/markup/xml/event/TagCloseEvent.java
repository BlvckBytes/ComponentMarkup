package at.blvckbytes.component_markup.markup.xml.event;

import org.jetbrains.annotations.Nullable;

public class TagCloseEvent extends XmlEvent {

  public final @Nullable String tagName;

  public TagCloseEvent(@Nullable String tagName) {
    this.tagName = tagName;
  }
}
