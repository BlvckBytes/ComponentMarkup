/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.util.StringView;

public class TagAttributeEndEvent implements XmlEvent {

  public final StringView name;

  public TagAttributeEndEvent(StringView name) {
    this.name = name;
  }
}
