/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.InputView;

public class BooleanAttributeEvent implements XmlEvent {

  public final InputView name;
  public final String nameBuildResult;
  public final InputView raw;
  public final String rawBuildResult;

  public final boolean value;

  public BooleanAttributeEvent(InputView name, InputView raw, boolean value, String nameBuildResult, String rawBuildResult) {
    this.name = name;
    this.nameBuildResult = nameBuildResult;
    this.raw = raw;
    this.rawBuildResult = rawBuildResult;
    this.value = value;
  }
}
