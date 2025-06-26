package at.blvckbytes.component_markup.expression.tokenizer;

public enum Punctuation {
  OPENING_PARENTHESIS("("),
  CLOSING_PARENTHESIS(")"),
  CLOSING_BRACKET("]"),
  COMMA(","),
  COLON(":"),
  ;

  private final String representation;
  public final int length;

  Punctuation(String representation) {
    this.representation = representation;
    this.length = representation.length();
  }

  @Override
  public String toString() {
    return representation;
  }
}
