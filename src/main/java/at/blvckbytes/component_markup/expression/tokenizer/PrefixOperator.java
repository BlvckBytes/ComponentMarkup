package at.blvckbytes.component_markup.expression.tokenizer;

public enum PrefixOperator {
  NEGATION("!"),
  FLIP_SIGN("-"),
  ;

  private final String representation;

  PrefixOperator(String representation) {
    this.representation = representation;
  }

  @Override
  public String toString() {
    return representation;
  }
}
