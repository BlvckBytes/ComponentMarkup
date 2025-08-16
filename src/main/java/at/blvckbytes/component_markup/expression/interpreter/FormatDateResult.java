/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import java.util.EnumSet;

public class FormatDateResult {

  public final String formattedString;
  public final EnumSet<FormatDateWarning> errors;

  public FormatDateResult(String formattedString, EnumSet<FormatDateWarning> errors) {
    this.formattedString = formattedString;
    this.errors = errors;
  }
}
