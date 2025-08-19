/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.coordinates;

public enum CoordinatesParseError {
  MALFORMED_NUMBER("This number is malformed"),
  EXPECTED_X_COORDINATE("Expected the x-coordinate number"),
  EXPECTED_Y_COORDINATE("Expected the y-coordinate number"),
  EXPECTED_Z_COORDINATE("Expected the z-coordinate number"),
  EXPECTED_END_AFTER_WORLD_NAME("Expected there to be no more content after the optional world-name"),
  ;

  public final String message;

  CoordinatesParseError(String message) {
    this.message = message;
  }
}
