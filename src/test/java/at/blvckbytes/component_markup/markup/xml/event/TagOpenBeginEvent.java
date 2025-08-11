/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class TagOpenBeginEvent implements XmlEvent {

  public final StringView tagName;
  public final String tagNameBuildResult;

  public TagOpenBeginEvent(StringView tagName, String tagNameBuildResult) {
    this.tagName = tagName;
    this.tagNameBuildResult = tagNameBuildResult;
  }
}
