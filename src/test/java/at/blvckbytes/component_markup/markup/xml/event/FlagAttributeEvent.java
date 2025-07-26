package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class FlagAttributeEvent extends XmlEvent {

  public final StringView name;
  public final String nameBuildResult;

  public FlagAttributeEvent(StringView name, String nameBuildResult) {
    this.name = name;
    this.nameBuildResult = nameBuildResult;
  }
}
