/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.ErrorMessage;

public class ExpressionTokenizeException extends RuntimeException implements ErrorMessage {

  public final int position;
  public final ExpressionTokenizeError error;
  public final String[] messagePlaceholders;

  public ExpressionTokenizeException(int position, ExpressionTokenizeError error, String... messagePlaceholders) {
    this.position = position;
    this.error = error;
    this.messagePlaceholders = messagePlaceholders;
  }

  @Override
  public String getErrorMessage() {
    return error.messageBuilder.apply(messagePlaceholders);
  }
}
