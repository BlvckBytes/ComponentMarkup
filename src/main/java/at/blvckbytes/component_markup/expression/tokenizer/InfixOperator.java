/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.StringView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum InfixOperator implements EnumToken {
  BRANCHING            ("?",   1, false),
  DISJUNCTION          ("||",  2, false),
  CONJUNCTION          ("&&",  3, false),
  EQUAL_TO             ("==",  4, false),
  NOT_EQUAL_TO         ("!=",  4, false),
  CONTAINS             ("::",  4, false),
  MATCHES_REGEX        (":::", 4, false),
  GREATER_THAN         (">",   5, false),
  GREATER_THAN_OR_EQUAL(">=",  5, false),
  LESS_THAN            ("<",   5, false),
  LESS_THAN_OR_EQUAL   ("<=",  5, false),
  CONCATENATION        ("&",   6, false),
  RANGE                ("..",  7, false),
  ADDITION             ("+",   8, false),
  SUBTRACTION          ("-",   8, false),
  MULTIPLICATION       ("*",   9, false),
  DIVISION             ("/",   9, false),
  MODULO               ("%",   9, false),
  EXPONENTIATION       ("^",  10, true),
  EXPLODE              ("@",  11, false),
  EXPLODE_REGEX        ("@@", 11, false),
  REPEAT               ("**", 11, false),
  FALLBACK             ("??", 12, false),
  SUBSCRIPTING         ("[",  13, false),
  MEMBER               (".",  13, false),
  ;

  public static final List<InfixOperator> CONTAINING_PIPE = Collections.singletonList(
    DISJUNCTION
  );

  public static final List<InfixOperator> CONTAINING_EQUALS = Arrays.asList(
    EQUAL_TO, NOT_EQUAL_TO, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL
  );

  public final String representation;

  // Higher precedence means is evaluated *earlier*.
  // Assuming the following input: "5 + 3 >= 2 + 1", the
  // operator + is evaluated before the operator >= is, so the
  // former has precedence relative to the latter, thus a higher number.
  public final int precedence;

  public final boolean rightAssociative;

  InfixOperator(String representation, int precedence, boolean rightAssociative) {
    this.representation = representation;
    this.precedence = precedence;
    this.rightAssociative = rightAssociative;
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
}
