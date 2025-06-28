package at.blvckbytes.component_markup.expression.parser;

public class ExpressionParseException extends RuntimeException {

  public final ExpressionParserError error;
  public final int charIndex;

  public ExpressionParseException(ExpressionParserError error, int charIndex) {
    this.error = error;
    this.charIndex = charIndex;
  }
}
