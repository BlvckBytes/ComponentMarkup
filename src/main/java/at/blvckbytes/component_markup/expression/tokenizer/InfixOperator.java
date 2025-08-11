/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum InfixOperator implements EnumToken {
  BRANCHING_THEN       ("then",     1, OperatorFlag.NAMED),
  BRANCHING_ELSE       ("else",     1, OperatorFlag.NAMED),
  DISJUNCTION          ("or",       2, OperatorFlag.NAMED),
  CONJUNCTION          ("and",      3, OperatorFlag.NAMED),
  EQUAL_TO             ("eq",       4, OperatorFlag.NAMED),
  NOT_EQUAL_TO         ("neq",      4, OperatorFlag.NAMED),
  IN                   ("in",       4, OperatorFlag.NAMED),
  MATCHES_REGEX        ("matches",  4, OperatorFlag.NAMED),
  GREATER_THAN         (">",        5),
  GREATER_THAN_OR_EQUAL(">=",       5),
  LESS_THAN            ("<",        5),
  LESS_THAN_OR_EQUAL   ("<=",       5),
  CONCATENATION        ("&",        6),
  RANGE                ("..",       7),
  ADDITION             ("+",        8),
  SUBTRACTION          ("-",        8),
  MULTIPLICATION       ("*",        9),
  DIVISION             ("/",        9),
  MODULO               ("%",        9),
  EXPONENTIATION       ("^",       10, OperatorFlag.RIGHT_ASSOCIATIVE),
  SPLIT                ("split",   11, OperatorFlag.NAMED),
  REGEX_SPLIT          ("rsplit",  11, OperatorFlag.NAMED),
  REPEAT               ("**",      11),
  FALLBACK             ("??",      12),
  SUBSCRIPTING         ("[",       13),
  MEMBER               (".",       13),
  ;

  public static final Set<String> RESERVED_NAMES;
  private static final Map<String, InfixOperator> OPERATOR_BY_NAME;

  static {
    OPERATOR_BY_NAME = new HashMap<>();

    for (InfixOperator operator : values()) {
      if (operator.flags.contains(OperatorFlag.NAMED))
        OPERATOR_BY_NAME.put(operator.representation, operator);
    }

    RESERVED_NAMES = OPERATOR_BY_NAME.keySet();
  }

  public final String representation;

  // Higher precedence means is evaluated *earlier*.
  // Assuming the following input: "5 + 3 gt 2 + 1", the
  // operator + is evaluated before the operator gt is, so the
  // former has precedence relative to the latter, thus a higher number.
  public final int precedence;

  public final EnumSet<OperatorFlag> flags;

  InfixOperator(String representation, int precedence, OperatorFlag... flags) {
    this.representation = representation;
    this.precedence = precedence;
    this.flags = flags.length == 0 ? EnumSet.noneOf(OperatorFlag.class) : EnumSet.of(flags[0], flags);
  }

  @Override
  public String toString() {
    return representation;
  }

  @Override
  public Token create(StringView raw) {
    return new InfixOperatorToken(raw, this);
  }

  @Override
  public int getLength() {
    return representation.length();
  }

  public static @Nullable InfixOperator byName(String name) {
    return OPERATOR_BY_NAME.get(name);
  }
}
