/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.util.StringView;

public class SelectorParseException extends RuntimeException implements ErrorMessage {

  public final StringView input;
  public final int position;
  public final SelectorParseError error;
  private final String[] messagePlaceholders;

  public SelectorParseException(StringView input, SelectorParseError error, String... messagePlaceholders) {
    this(input, Math.max(0, input.getPosition()), error, messagePlaceholders);
  }

  public SelectorParseException(StringView input, int position, SelectorParseError error, String... messagePlaceholders) {
    this.input = input;
    this.position = position;
    this.error = error;
    this.messagePlaceholders = messagePlaceholders;
  }

  @Override
  public String getErrorMessage() {
    return error.messageBuilder.apply(messagePlaceholders);
  }
}
