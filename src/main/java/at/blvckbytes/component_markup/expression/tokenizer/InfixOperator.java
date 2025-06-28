package at.blvckbytes.component_markup.expression.tokenizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum InfixOperator {
  BRANCHING            ("?",   1, false),
  DISJUNCTION          ("||",  2, false),
  CONJUNCTION          ("&&",  3, false),
  EQUAL_TO             ("==",  4, false),
  NOT_EQUAL_TO         ("!=",  4, false),
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

  private final String representation;
  public final int length;

  // Higher precedence means is evaluated *earlier*.
  // Assuming the following input: "5 + 3 >= 2 + 1", the
  // operator + is evaluated before the operator >= is, so the
  // former has precedence relative to the latter, thus a higher number.
  public final int precedence;

  public final boolean rightAssociative;

  InfixOperator(String representation, int precedence, boolean rightAssociative) {
    this.representation = representation;
    this.length = representation.length();
    this.precedence = precedence;
    this.rightAssociative = rightAssociative;
  }

  @Override
  public String toString() {
    return representation;
  }
}
