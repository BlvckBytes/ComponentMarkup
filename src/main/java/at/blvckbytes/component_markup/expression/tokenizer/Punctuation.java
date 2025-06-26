package at.blvckbytes.component_markup.expression.tokenizer;

public enum Punctuation {
  OPENING_PARENTHESIS("("),
  CLOSING_PARENTHESIS(")"),
  CLOSING_BRACKET("]"),
  COMMA(","),
  COLON(":"),
  ;

  private final String representation;

  Punctuation(String representation) {
    this.representation = representation;
  }

  @Override
  public String toString() {
    return representation;
  }
}
