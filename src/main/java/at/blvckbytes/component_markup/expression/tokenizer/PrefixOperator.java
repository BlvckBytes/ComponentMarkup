/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum PrefixOperator implements EnumToken {
  NEGATION   ("not",     true, false),
  FLIP_SIGN  ("-",      false, false),
  UPPER_CASE ("upper",   true,  true),
  LOWER_CASE ("lower",   true,  true),
  TITLE_CASE ("title",   true,  true),
  TOGGLE_CASE("toggle",  true,  true),
  SLUGIFY    ("slugify", true,  true),
  ASCIIFY    ("asciify", true,  true),
  TRIM       ("trim",    true,  true),
  REVERSE    ("reverse", true,  true),
  LONG       ("long",    true,  true),
  DOUBLE     ("double",  true,  true),
  FLOOR      ("floor",   true,  true),
  CEIL       ("ceil",    true,  true),
  ROUND      ("round",   true,  true),
  ;

  public static final Set<String> RESERVED_NAMES;

  static {
    RESERVED_NAMES = Arrays.stream(values())
      .filter(it -> it.isNamed && !it.requiresParentheses)
      .map(it -> it.representation)
      .collect(Collectors.toSet());
  }

  public final String representation;
  public final boolean isNamed;
  public final boolean requiresParentheses;

  PrefixOperator(String representation, boolean isNamed, boolean requiresParentheses) {
    this.representation = representation;
    this.isNamed = isNamed;
    this.requiresParentheses = requiresParentheses;
  }

  @Override
  public String toString() {
    return representation;
  }

  @Override
  public Token create(StringView raw) {
    return new PrefixOperatorToken(raw, this);
  }

  @Override
  public int getLength() {
    return representation.length();
  }

  public static @Nullable PrefixOperator byName(String name) {
    switch (name) {
      case "not":
        return NEGATION;
      case "upper":
        return UPPER_CASE;
      case "lower":
        return LOWER_CASE;
      case "title":
        return TITLE_CASE;
      case "toggle":
        return TOGGLE_CASE;
      case "slugify":
        return SLUGIFY;
      case "asciify":
        return ASCIIFY;
      case "trim":
        return TRIM;
      case "reverse":
        return REVERSE;
      case "long":
        return LONG;
      case "double":
        return DOUBLE;
      case "floor":
        return FLOOR;
      case "ceil":
        return CEIL;
      case "round":
        return ROUND;
      default:
        return null;
    }
  }
}
