/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class StringAttributeEvent implements XmlEvent {

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
