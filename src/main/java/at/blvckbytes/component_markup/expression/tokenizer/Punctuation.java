package at.blvckbytes.component_markup.expression.tokenizer;

public enum Punctuation {
  OPENING_PARENTHESIS('('),
  CLOSING_PARENTHESIS(')'),
  CLOSING_BRACKET(']'),
  COMMA(','),
  COLON(':'),
  ;

  public final char representation;

  Punctuation(char representation) {
    this.representation = representation;
  }

  @Override
  public String toString() {
    return String.valueOf(representation);
  }
}
