package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.Jsonifiable;
import org.jetbrains.annotations.Nullable;

public class TagCloseEvent extends Jsonifiable implements XmlEvent {

  public final @Nullable String tagName;

  public TagCloseEvent(@Nullable String tagName) {
    this.tagName = tagName;
  }
}
