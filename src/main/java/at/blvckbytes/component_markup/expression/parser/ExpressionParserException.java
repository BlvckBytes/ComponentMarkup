package at.blvckbytes.component_markup.expression.parser;

public class ExpressionParserException extends RuntimeException {

  public final ExpressionParserError error;
  public final int charIndex;

  public ExpressionParserException(ExpressionParserError error, int charIndex) {
    this.error = error;
    this.charIndex = charIndex;
  }
}
