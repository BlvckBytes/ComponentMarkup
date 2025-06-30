package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.ErrorMessage;

public class ExpressionTokenizeException extends RuntimeException implements ErrorMessage {

  public final int beginIndex;
  public final ExpressionTokenizeError error;

  public ExpressionTokenizeException(int beginIndex, ExpressionTokenizeError error) {
    this.beginIndex = beginIndex;
    this.error = error;
  }

  @Override
  public String getErrorMessage() {
    // TODO: Placeholders
    return error.getErrorMessage();
  }
}
