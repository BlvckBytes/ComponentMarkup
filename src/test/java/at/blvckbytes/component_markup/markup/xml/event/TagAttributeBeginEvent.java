/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class TagAttributeBeginEvent implements XmlEvent {

  public final StringView name;
  public final int valueBeginPosition;
  public final String nameBuildResult;

  public TagAttributeBeginEvent(StringView name, int valueBeginPosition, String nameBuildResult) {
    this.name = name;
    this.valueBeginPosition = valueBeginPosition;
    this.nameBuildResult = nameBuildResult;
  }
}
