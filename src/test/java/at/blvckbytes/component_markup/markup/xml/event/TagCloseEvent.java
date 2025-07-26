package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public class TagCloseEvent extends XmlEvent {

  public final @Nullable StringView tagName;
  public final @Nullable String tagNameBuildResult;
  public final StringPosition pointyPosition;

  public TagCloseEvent(@Nullable StringView tagName, StringPosition pointyPosition, @Nullable String tagNameBuildResult) {
    this.tagName = tagName;
    this.tagNameBuildResult = tagNameBuildResult;
    this.pointyPosition = pointyPosition;
  }
}
