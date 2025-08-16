/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.InputView;

public class TagOpenEndEvent implements XmlEvent {

  public final InputView tagName;
  public final boolean wasSelfClosing;

  public TagOpenEndEvent(InputView tagName, boolean wasSelfClosing) {
    this.tagName = tagName;
    this.wasSelfClosing = wasSelfClosing;
  }
}
