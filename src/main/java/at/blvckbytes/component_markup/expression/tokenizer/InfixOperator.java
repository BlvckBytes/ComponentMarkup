/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

public enum InfixOperator implements EnumToken {
  BRANCHING            ("?",        1, false, false),
  DISJUNCTION          ("or",       2, false, true),
  CONJUNCTION          ("and",      3, false, true),
  EQUAL_TO             ("eq",       4, false, true),
  NOT_EQUAL_TO         ("neq",      4, false, true),
  IN                   ("in",       4, false, true),
  MATCHES_REGEX        ("matches",  4, false, true),
  GREATER_THAN         (">",        5, false, false),
  GREATER_THAN_OR_EQUAL(">=",       5, false, false),
  LESS_THAN            ("<",        5, false, false),
  LESS_THAN_OR_EQUAL   ("<=",       5, false, false),
  CONCATENATION        ("&",        6, false, false),
  RANGE                ("..",       7, false, false),
  ADDITION             ("+",        8, false, false),
  SUBTRACTION          ("-",        8, false, false),
  MULTIPLICATION       ("*",        9, false, false),
  DIVISION             ("/",        9, false, false),
  MODULO               ("%",        9, false, false),
  EXPONENTIATION       ("^",       10,  true, false),
  EXPLODE              ("@",       11, false, false),
  EXPLODE_REGEX        ("@@",      11, false, false),
  REPEAT               ("**",      11, false, false),
  FALLBACK             ("??",      12, false, false),
  SUBSCRIPTING         ("[",       13, false, false),
  MEMBER               (".",       13, false, false),
  ;

  public final String representation;

  // Higher precedence means is evaluated *earlier*.
  // Assuming the following input: "5 + 3 gt 2 + 1", the
  // operator + is evaluated before the operator gt is, so the
  // former has precedence relative to the latter, thus a higher number.
  public final int precedence;

  public final boolean rightAssociative;

  public final boolean isNamed;

  InfixOperator(String representation, int precedence, boolean rightAssociative, boolean isNamed) {
    this.representation = representation;
    this.precedence = precedence;
    this.rightAssociative = rightAssociative;
    this.isNamed = isNamed;
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
    switch (name) {
      case "or":
        return DISJUNCTION;
      case "and":
        return CONJUNCTION;
      case "eq":
        return EQUAL_TO;
      case "neq":
        return NOT_EQUAL_TO;
      case "in":
        return IN;
      case "matches":
        return MATCHES_REGEX;
      default:
        return null;
    }
  }
}
