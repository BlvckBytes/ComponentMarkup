/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.InputView;

public class TagOpenBeginEvent implements XmlEvent {

  public final InputView tagName;
  public final String tagNameBuildResult;

  public TagOpenBeginEvent(InputView tagName, String tagNameBuildResult) {
    this.tagName = tagName;
    this.tagNameBuildResult = tagNameBuildResult;
  }
}
