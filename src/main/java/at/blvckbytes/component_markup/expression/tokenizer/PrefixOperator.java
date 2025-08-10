/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public enum PrefixOperator implements EnumToken {
  NEGATION   ("!", false),
  FLIP_SIGN  ("-", false),
  UPPER_CASE ("upper", true),
  LOWER_CASE ("lower", true),
  TITLE_CASE ("title", true),
  TOGGLE_CASE("toggle", true),
  SLUGIFY    ("slugify", true),
  ASCIIFY    ("asciify", true),
  TRIM       ("trim", true),
  REVERSE    ("reverse", true),
  ;

  public final String representation;
  public final boolean isNamed;

  PrefixOperator(String representation, boolean isNamed) {
    this.representation = representation;
    this.isNamed = isNamed;
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
      default:
        return null;
    }
  }
}
