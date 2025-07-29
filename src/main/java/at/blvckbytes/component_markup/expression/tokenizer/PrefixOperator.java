package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.StringView;

import java.util.Arrays;
import java.util.List;

public enum PrefixOperator implements EnumToken {
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

  @Override
  public Token create(StringView raw) {
    return new PrefixOperatorToken(raw, this);
  }
}
