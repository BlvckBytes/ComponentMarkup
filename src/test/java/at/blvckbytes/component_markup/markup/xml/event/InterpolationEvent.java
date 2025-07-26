package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class InterpolationEvent extends XmlEvent {

  public final StringView expression;
  public final String expressionBuildResult;

  public InterpolationEvent(StringView expression, String expressionBuildResult) {
    this.expression = expression;
    this.expressionBuildResult = expressionBuildResult;
  }
}
