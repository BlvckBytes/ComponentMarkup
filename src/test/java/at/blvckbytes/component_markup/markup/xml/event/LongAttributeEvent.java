/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class LongAttributeEvent extends XmlEvent {

  public final StringView name;
  public final String nameBuildResult;
  public final StringView raw;
  public final String rawBuildResult;

  public final long value;

  public LongAttributeEvent(StringView name, StringView raw, long value, String nameBuildResult, String rawBuildResult) {
    this.name = name;
    this.nameBuildResult = nameBuildResult;
    this.raw = raw;
    this.rawBuildResult = rawBuildResult;
    this.value = value;
  }
}
