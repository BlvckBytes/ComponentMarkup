package at.blvckbytes.component_markup.expression.tokenizer;

public enum Punctuation {
  OPENING_PARENTHESIS("("),
  CLOSING_PARENTHESIS(")"),
  OPENING_BRACKET("["),
  CLOSING_BRACKET("]"),
  COMMA(","),
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
