package at.blvckbytes.component_markup.expression.tokenizer;

public enum PrefixOperator {
  NEGATION("!"),
  FLIP_SIGN("-"),
  ;

  private final String representation;
  public final int length;

  PrefixOperator(String representation) {
    this.representation = representation;
    this.length = representation.length();
  }

  @Override
  public String toString() {
    return representation;
  }
}
