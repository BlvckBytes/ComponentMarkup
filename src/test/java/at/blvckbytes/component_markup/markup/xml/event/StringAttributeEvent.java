package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class StringAttributeEvent extends XmlEvent {

  public final StringView name;
  public final String nameBuildResult;
  public final StringView value;
  public final String rawBuildResult;

  public StringAttributeEvent(StringView name, StringView value, String nameBuildResult, String rawBuildResult) {
    this.name = name;
    this.nameBuildResult = nameBuildResult;
    this.value = value;
    this.rawBuildResult = rawBuildResult;
  }
}
