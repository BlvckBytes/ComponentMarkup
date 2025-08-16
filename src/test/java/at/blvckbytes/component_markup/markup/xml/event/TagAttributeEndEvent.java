/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.InputView;

public class TagAttributeEndEvent implements XmlEvent {

  public final InputView name;

  public TagAttributeEndEvent(InputView name) {
    this.name = name;
  }
}
