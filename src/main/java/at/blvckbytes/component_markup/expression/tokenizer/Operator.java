package at.blvckbytes.component_markup.expression.tokenizer;

public enum Operator {
  ADDITION("+"),
  SUBTRACTION("-"),
  MULTIPLICATION("*"),
  DIVISION("/"),
  MODULO("%"),
  EXPONENTIATION("^"),
  CONCATENATION("&"),
  TERNARY_BEGIN("?"),
  TERNARY_DELIMITER(":"),
  GREATER_THAN(">"),
  GREATER_THAN_OR_EQUAL(">="),
  LESS_THAN("<"),
  LESS_THAN_OR_EQUAL("<="),
  EQUAL_TO("=="),
  NOT_EQUAL_TO("!="),
  RANGE(".."),
  MEMBER("."),
  NEGATION("!"),
  CONJUNCTION("&&"),
  DISJUNCTION("||"),
  NULL_COALESCE("??")
  ;

  private final String representation;

  Operator(String representation) {
    this.representation = representation;
  }

  @Override
  public String toString() {
    return representation;
  }
}
