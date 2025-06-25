package at.blvckbytes.component_markup.expression.tokenizer;

public class ExpressionTokenizeException extends RuntimeException {

  public final int beginIndex;
  public final ExpressionTokenizeError error;

  public ExpressionTokenizeException(int beginIndex, ExpressionTokenizeError error) {
    this.beginIndex = beginIndex;
    this.error = error;
  }
}
