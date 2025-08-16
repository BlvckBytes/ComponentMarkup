/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.InputView;

public class TagAttributeBeginEvent implements XmlEvent {

  public final InputView name;
  public final int valueBeginPosition;
  public final String nameBuildResult;

  public TagAttributeBeginEvent(InputView name, int valueBeginPosition, String nameBuildResult) {
    this.name = name;
    this.valueBeginPosition = valueBeginPosition;
    this.nameBuildResult = nameBuildResult;
  }
}
