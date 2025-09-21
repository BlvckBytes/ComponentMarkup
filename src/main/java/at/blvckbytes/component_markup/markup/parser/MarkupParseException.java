/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.markup.cml.CmlParseException;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.MessagePlaceholders;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MarkupParseException extends RuntimeException implements ErrorMessage {

  private @Nullable InputView rootView;

  public final int position;
  public final MarkupParseError error;
  public final String[] messagePlaceholders;

  public MarkupParseException(InputView positionProvider, MarkupParseError error, String... messagePlaceholders) {
    this(positionProvider.startInclusive, error, messagePlaceholders);
  }

  public MarkupParseException(int position, MarkupParseError error, String... messagePlaceholders) {
    this.position = position;
    this.error = error;
    this.messagePlaceholders = messagePlaceholders;
  }

  public MarkupParseException(CmlParseException cmlException) {
    super(cmlException);

    this.position = cmlException.position;
    this.error = MarkupParseError.CML_PARSE_ERROR;
    this.messagePlaceholders = new String[0];
  }

  public MarkupParseException(int position, ExpressionParseException expressionParseException) {
    super(expressionParseException);

    this.position = position;
    this.error = MarkupParseError.EXPRESSION_PARSE_ERROR;
    this.messagePlaceholders = new String[0];
  }

  public MarkupParseException(int position, ExpressionTokenizeException expressionTokenizeException) {
    super(expressionTokenizeException);

    this.position = position;
    this.error = MarkupParseError.EXPRESSION_TOKENIZE_ERROR;
    this.messagePlaceholders = new String[0];
  }

  @Override
  public String getErrorMessage() {
    switch (this.error) {
      case CML_PARSE_ERROR:
        return ((CmlParseException) getCause()).getErrorMessage();

      case EXPRESSION_PARSE_ERROR:
        return ((ExpressionParseException) getCause()).getErrorMessage();

      case EXPRESSION_TOKENIZE_ERROR:
        return ((ExpressionTokenizeException) getCause()).getErrorMessage();

      default:
        return this.error.messageBuilder.apply(new MessagePlaceholders(messagePlaceholders));
    }
  }

  public int getCharIndex() {
    switch (this.error) {
      case EXPRESSION_PARSE_ERROR:
        return ((ExpressionParseException) getCause()).position;

      case EXPRESSION_TOKENIZE_ERROR:
        return ((ExpressionTokenizeException) getCause()).position;
    }

    return position;
  }

  public MarkupParseException setRootView(InputView rootView) {
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

    return ErrorScreen.make(rootView.contents, getCharIndex(), getErrorMessage());
  }
}
