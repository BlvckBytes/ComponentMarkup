/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.PrefixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
  MAX        ("max",     OperatorFlag.NAMED, OperatorFlag.PARENS, OperatorFlag.VARIADIC),
  MIN        ("min",     OperatorFlag.NAMED, OperatorFlag.PARENS, OperatorFlag.VARIADIC),
  AVG        ("avg",     OperatorFlag.NAMED, OperatorFlag.PARENS, OperatorFlag.VARIADIC),
  SUM        ("sum",     OperatorFlag.NAMED, OperatorFlag.PARENS, OperatorFlag.VARIADIC),
  LEN        ("len",     OperatorFlag.NAMED, OperatorFlag.PARENS),
  HAS        ("has",     OperatorFlag.NAMED, OperatorFlag.PARENS),
  ENV        ("env",     OperatorFlag.NAMED, OperatorFlag.PARENS),
  ;

  public static final Set<String> RESERVED_NAMES;
  private static final Map<String, PrefixOperator> OPERATOR_BY_NAME;

  static {
    RESERVED_NAMES = new HashSet<>();
    OPERATOR_BY_NAME = new HashMap<>();

    for (PrefixOperator operator : values()) {
      if (!operator.flags.contains(OperatorFlag.NAMED))
        continue;

      OPERATOR_BY_NAME.put(operator.representation, operator);

      if (!operator.flags.contains(OperatorFlag.PARENS))
        RESERVED_NAMES.add(operator.representation);
    }
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
  public Token create(InputView raw) {
    return new PrefixOperatorToken(raw, this);
  }

  @Override
  public int getLength() {
    return representation.length();
  }

  public static @Nullable PrefixOperator byName(String name) {
    return OPERATOR_BY_NAME.get(name);
  }
}
