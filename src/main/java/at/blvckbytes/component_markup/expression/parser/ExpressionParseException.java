package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.ErrorMessage;

public class ExpressionParseException extends RuntimeException implements ErrorMessage {

  public final ExpressionParserError error;
  public final int charIndex;
  private final Object[] messagePlaceholders;

  public ExpressionParseException(ExpressionParserError error, int charIndex, Object... messagePlaceholders) {
    this.error = error;
    this.charIndex = charIndex;
    this.messagePlaceholders = messagePlaceholders;
  }

  @Override
  public String getErrorMessage() {
    return String.format(error.getErrorMessage(), messagePlaceholders);
  }
}
