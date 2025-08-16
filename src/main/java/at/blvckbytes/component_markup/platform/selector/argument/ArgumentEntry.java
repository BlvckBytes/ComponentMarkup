/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

import at.blvckbytes.component_markup.util.InputView;

public class ArgumentEntry {

  public final ArgumentName name;
  public final InputView rawName;
  public final ArgumentValue value;

  public ArgumentEntry(ArgumentName name, InputView rawName, ArgumentValue value) {
    this.name = name;
    this.rawName = rawName;
    this.value = value;
  }
}
