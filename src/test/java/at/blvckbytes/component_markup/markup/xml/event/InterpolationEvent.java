/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class InterpolationEvent implements XmlEvent {

  public final StringView expression;
  public final String expressionBuildResult;

  public InterpolationEvent(StringView expression, String expressionBuildResult) {
    this.expression = expression;
    this.expressionBuildResult = expressionBuildResult;
  }
}
