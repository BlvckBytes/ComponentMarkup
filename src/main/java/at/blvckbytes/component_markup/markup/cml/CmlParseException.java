/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml;

import at.blvckbytes.component_markup.util.ErrorMessage;

public class CmlParseException extends RuntimeException implements ErrorMessage {

  public final CmlParseError error;
  public final int position;

  public CmlParseException(CmlParseError error, int position) {
    this.error = error;
    this.position = position;
  }

  @Override
  public String getErrorMessage() {
    return error.getErrorMessage();
  }
}
