/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import java.util.EnumSet;

public class FormatNumberResult {

  public final String formattedString;
  public final EnumSet<FormatNumberWarning> warnings;

  public FormatNumberResult(String formattedString, EnumSet<FormatNumberWarning> warnings) {
    this.formattedString = formattedString;
    this.warnings = warnings;
  }
}
