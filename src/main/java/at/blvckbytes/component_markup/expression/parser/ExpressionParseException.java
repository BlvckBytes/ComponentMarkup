/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.util.ErrorMessage;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.MessagePlaceholders;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExpressionParseException extends RuntimeException implements ErrorMessage {

  private @Nullable InputView rootView;

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

  public ExpressionParseException setRootView(InputView rootView) {
    if (this.rootView != null)
      throw new IllegalStateException("Root-view was already set");

    if (rootView == null)
      throw new IllegalStateException("Do not set a null-value");

    this.rootView = rootView;
    return this;
  }

  public List<String> makeErrorScreen() {
    if (rootView == null)
      throw new IllegalStateException("Have not been provided with a reference to the root-view");

    return ErrorScreen.make(rootView, position, getErrorMessage());
  }
}
