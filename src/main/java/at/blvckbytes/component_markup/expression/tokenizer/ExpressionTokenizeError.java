package at.blvckbytes.component_markup.expression.tokenizer;

public enum ExpressionTokenizeError {
  UNTERMINATED_STRING,
  MALFORMED_IDENTIFIER,
  EXPECTED_DECIMAL_DIGITS,
  SINGLE_EQUALS,
  SINGLE_TILDE,
  SINGLE_PIPE,
}
