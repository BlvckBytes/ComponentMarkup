package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.ErrorMessage;

import java.util.stream.Collectors;

public enum ExpressionTokenizeError implements ErrorMessage {
  UNTERMINATED_STRING("This string misses its closing-sequence: '"),
  MALFORMED_IDENTIFIER("This name is malformed: cannot start with digits or underscores, may only contain a-z, 0-9 and underscores"),
  EXPECTED_DECIMAL_DIGITS("Expected there to be digits after the decimal-dot (.)"),
  SINGLE_EQUALS(
    "Encountered a single equals-sign (=); use one of: " +
      InfixOperator.CONTAINING_EQUALS.stream()
        .map(InfixOperator::toString)
        .collect(Collectors.joining(", "))
  ),
  SINGLE_TILDE(
    "Encountered a single tilde (~); use one of: " +
      PrefixOperator.CONTAINING_TILDE.stream()
        .map(PrefixOperator::toString)
        .collect(Collectors.joining(", "))
  ),
  SINGLE_PIPE(
    "Encountered a single pipe (|); use one of: " +
      InfixOperator.CONTAINING_PIPE.stream()
        .map(InfixOperator::toString)
        .collect(Collectors.joining(", "))
  ),
  ;

  private final String message;

  ExpressionTokenizeError(String message) {
    this.message = message;
  }

  @Override
  public String getErrorMessage() {
    return this.message;
  }
}
