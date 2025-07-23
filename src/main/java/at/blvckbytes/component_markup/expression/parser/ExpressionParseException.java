package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.ErrorMessage;

public class ExpressionParseException extends RuntimeException implements ErrorMessage {

  public final ExpressionParserError error;
  public final int charIndex;
  private final String[] messagePlaceholders;

  public ExpressionParseException(ExpressionParserError error, int charIndex, String... messagePlaceholders) {
    this.error = error;
    this.charIndex = charIndex;
    this.messagePlaceholders = messagePlaceholders;
  }

  @Override
  public String getErrorMessage() {
    return error.messageBuilder.apply(messagePlaceholders);
  }
}
