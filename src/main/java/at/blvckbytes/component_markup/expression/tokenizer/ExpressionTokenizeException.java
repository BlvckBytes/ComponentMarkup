package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.ErrorMessage;

public class ExpressionTokenizeException extends RuntimeException implements ErrorMessage {

  public final int position;
  public final ExpressionTokenizeError error;

  public ExpressionTokenizeException(int position, ExpressionTokenizeError error) {
    this.position = position;
    this.error = error;
  }

  @Override
  public String getErrorMessage() {
    return error.getErrorMessage();
  }
}
