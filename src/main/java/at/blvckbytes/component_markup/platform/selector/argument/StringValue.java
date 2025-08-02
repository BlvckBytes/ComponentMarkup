/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector.argument;

import at.blvckbytes.component_markup.util.StringView;

public class StringValue implements ArgumentValue {

  public final StringView value;
  public final boolean negated;

  public StringValue(StringView value, boolean negated) {
    this.value = value;
    this.negated = negated;
  }
}
