package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.util.StringPosition;

public class ExpressionParseException extends RuntimeException implements ErrorMessage {

  public final StringPosition position;
  public final ExpressionParserError error;
  private final String[] messagePlaceholders;

  public ExpressionParseException(StringPosition position, ExpressionParserError error, String... messagePlaceholders) {
    this.position = position;
    this.error = error;
    this.messagePlaceholders = messagePlaceholders;
  }

  @Override
  public String getErrorMessage() {
    return error.messageBuilder.apply(messagePlaceholders);
  }
}
