package at.blvckbytes.component_markup.expression.tokenizer;

public enum PrefixOperator {
  NEGATION   ("!"),
  FLIP_SIGN  ("-"),
  UPPER_CASE ("~^"),
  LOWER_CASE ("~_"),
  TITLE_CASE ("~#"),
  TOGGLE_CASE("~!"),
  SLUGIFY    ("~-"),
  ASCIIFY    ("~?"),
  TRIM       ("~|"),
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
