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
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum PrefixOperator implements EnumToken {
  NEGATION   ("not",     OperatorFlag.NAMED),
  FLIP_SIGN  ("-"),
  UPPER_CASE ("upper",   OperatorFlag.NAMED, OperatorFlag.PARENS),
  LOWER_CASE ("lower",   OperatorFlag.NAMED, OperatorFlag.PARENS),
  TITLE_CASE ("title",   OperatorFlag.NAMED, OperatorFlag.PARENS),
  TOGGLE_CASE("toggle",  OperatorFlag.NAMED, OperatorFlag.PARENS),
  SLUGIFY    ("slugify", OperatorFlag.NAMED, OperatorFlag.PARENS),
  ASCIIFY    ("asciify", OperatorFlag.NAMED, OperatorFlag.PARENS),
  TRIM       ("trim",    OperatorFlag.NAMED, OperatorFlag.PARENS),
  REVERSE    ("reverse", OperatorFlag.NAMED, OperatorFlag.PARENS),
  LONG       ("long",    OperatorFlag.NAMED, OperatorFlag.PARENS),
  DOUBLE     ("double",  OperatorFlag.NAMED, OperatorFlag.PARENS),
  FLOOR      ("floor",   OperatorFlag.NAMED, OperatorFlag.PARENS),
  CEIL       ("ceil",    OperatorFlag.NAMED, OperatorFlag.PARENS),
  ROUND      ("round",   OperatorFlag.NAMED, OperatorFlag.PARENS),
  ;

  public static final Set<String> RESERVED_NAMES;

  static {
    RESERVED_NAMES = Arrays.stream(values())
      .filter(it -> it.flags.contains(OperatorFlag.NAMED) && !it.flags.contains(OperatorFlag.PARENS))
      .map(it -> it.representation)
      .collect(Collectors.toSet());
  }

  public final String representation;
  public final EnumSet<OperatorFlag> flags;

  PrefixOperator(String representation, OperatorFlag... flags) {
    this.representation = representation;
    this.flags = flags.length == 0 ? EnumSet.noneOf(OperatorFlag.class) : EnumSet.of(flags[0], flags);
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
