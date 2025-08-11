/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class TextEvent implements XmlEvent {

  public final StringView text;
  public final String textBuildResult;

  public TextEvent(StringView text, String textBuildResult) {
    this.text = text;
    this.textBuildResult = textBuildResult;
  }
}
