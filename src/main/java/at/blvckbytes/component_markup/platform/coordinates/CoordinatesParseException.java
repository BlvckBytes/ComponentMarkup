/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.coordinates;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.util.InputView;

public class CoordinatesParseException extends RuntimeException implements ErrorMessage {

  public final InputView input;
  public final int position;
  public final CoordinatesParseError error;

  public CoordinatesParseException(InputView input, int position, CoordinatesParseError error) {
    this.input = input;
    this.position = Math.max(0, position);
    this.error = error;
  }

  @Override
  public String getErrorMessage() {
    return error.message;
  }
}
