package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class TagCloseEvent extends XmlEvent {

  public final @Nullable StringView tagName;

  public TagCloseEvent(@Nullable StringView tagName) {
    this.tagName = tagName;
  }
}
