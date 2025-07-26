package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.util.StringPosition;

public class ExpressionTokenizeException extends RuntimeException implements ErrorMessage {

  public final StringPosition position;
  public final ExpressionTokenizeError error;

  public ExpressionTokenizeException(StringPosition position, ExpressionTokenizeError error) {
    this.position = position;
    this.error = error;
  }

  @Override
  public String getErrorMessage() {
    return error.getErrorMessage();
  }
}
