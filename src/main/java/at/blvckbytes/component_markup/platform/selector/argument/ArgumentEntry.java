/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

import at.blvckbytes.component_markup.util.StringView;

public class ArgumentEntry {

  public final ArgumentName name;
  public final StringView rawName;
  public final ArgumentValue value;

  public ArgumentEntry(ArgumentName name, StringView rawName, ArgumentValue value) {
    this.name = name;
    this.rawName = rawName;
    this.value = value;
  }
}
