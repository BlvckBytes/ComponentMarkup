package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;

public class TagAttributeBeginEvent extends XmlEvent {

  public final StringView name;
  public final StringPosition valueBeginPosition;
  public final String nameBuildResult;

  public TagAttributeBeginEvent(StringView name, StringPosition valueBeginPosition, String nameBuildResult) {
    this.name = name;
    this.valueBeginPosition = valueBeginPosition;
    this.nameBuildResult = nameBuildResult;
  }
}
