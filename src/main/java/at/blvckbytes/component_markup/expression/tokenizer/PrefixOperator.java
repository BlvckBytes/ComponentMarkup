package at.blvckbytes.component_markup.expression.tokenizer;

import java.util.Arrays;
import java.util.List;

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
  REVERSE    ("~<"),
  ;

  public static final List<PrefixOperator> CONTAINING_TILDE = Arrays.asList(
    UPPER_CASE, LOWER_CASE, TITLE_CASE, TOGGLE_CASE, SLUGIFY, ASCIIFY, TRIM, REVERSE
  );

  public final String representation;
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
