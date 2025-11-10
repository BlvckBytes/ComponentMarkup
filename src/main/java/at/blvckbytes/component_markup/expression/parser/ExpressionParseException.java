/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.util.ErrorMessage;
import at.blvckbytes.component_markup.util.MessagePlaceholders;

public class ExpressionParseException extends RuntimeException implements ErrorMessage {

  public final int position;
  public final ExpressionParserError error;
  private final String[] messagePlaceholders;

  public ExpressionParseException(int position, ExpressionParserError error, String... messagePlaceholders) {
    this.position = position;
    this.error = error;
    this.messagePlaceholders = messagePlaceholders;
  }

  @Override
  public String getErrorMessage() {
    return error.messageBuilder.apply(new MessagePlaceholders(messagePlaceholders));
  }
}
