/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml.event;

import at.blvckbytes.component_markup.util.InputView;

public class StringAttributeEvent implements CmlEvent {

  public final InputView name;
  public final String nameBuildResult;
  public final InputView value;
  public final String rawBuildResult;

  public StringAttributeEvent(InputView name, InputView value, String nameBuildResult, String rawBuildResult) {
    this.name = name;
    this.nameBuildResult = nameBuildResult;
    this.value = value;
    this.rawBuildResult = rawBuildResult;
  }
}
