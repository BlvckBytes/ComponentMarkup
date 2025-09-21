/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml.event;

import at.blvckbytes.component_markup.util.InputView;

public class TagOpenEndEvent implements CmlEvent {

  public final InputView tagName;
  public final boolean wasSelfClosing;

  public TagOpenEndEvent(InputView tagName, boolean wasSelfClosing) {
    this.tagName = tagName;
    this.wasSelfClosing = wasSelfClosing;
  }
}
