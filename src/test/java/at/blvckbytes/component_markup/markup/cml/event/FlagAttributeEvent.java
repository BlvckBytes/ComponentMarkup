/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml.event;

import at.blvckbytes.component_markup.util.InputView;

public class FlagAttributeEvent implements CmlEvent {

  public final InputView name;
  public final String nameBuildResult;

  public FlagAttributeEvent(InputView name, String nameBuildResult) {
    this.name = name;
    this.nameBuildResult = nameBuildResult;
  }
}
