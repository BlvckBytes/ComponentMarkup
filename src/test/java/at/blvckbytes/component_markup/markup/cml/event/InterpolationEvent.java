/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml.event;

import at.blvckbytes.component_markup.util.InputView;

public class InterpolationEvent implements CmlEvent {

  public final InputView expression;
  public final String expressionBuildResult;

  public InterpolationEvent(InputView expression, String expressionBuildResult) {
    this.expression = expression;
    this.expressionBuildResult = expressionBuildResult;
  }
}
