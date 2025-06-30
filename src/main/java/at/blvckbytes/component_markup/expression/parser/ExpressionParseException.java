package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.ErrorMessage;

public class ExpressionParseException extends RuntimeException implements ErrorMessage {

  public final ExpressionParserError error;
  public final int charIndex;

  public ExpressionParseException(ExpressionParserError error, int charIndex) {
    this.error = error;
    this.charIndex = charIndex;
  }

  @Override
  public String getErrorMessage() {
    // TODO: Placeholders
    return error.getErrorMessage();
  }
}
