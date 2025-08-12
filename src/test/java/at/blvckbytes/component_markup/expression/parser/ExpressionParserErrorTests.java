/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.OperatorFlag;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.StringView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExpressionParserErrorTests {

  @Test
  public void shouldThrowOnTrailingExpressions() {
    Object[] trailingTokens = { "c", Punctuation.OPENING_PARENTHESIS, 5, true };

    for (Object trailingToken : trailingTokens) {
      TextWithSubViews text = new TextWithSubViews(
        "a + b `" + trailingToken + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_EOS,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnArrayAfterSubscripting() {
    TextWithSubViews text = new TextWithSubViews(
      "a[0]`[´1, 2]"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMissingInfixRightOperand() {
    for (InfixOperator operator : InfixOperator.values()) {
      if (operator == InfixOperator.BRANCHING_ELSE)
        continue;

      TextWithSubViews text = new TextWithSubViews(
        "a `" + operator + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_RIGHT_INFIX_OPERAND,
        text.subView(0).endExclusive - 1
      );
    }
  }

  @Test
  public void shouldThrowOnExpectingSubstringUpperBound() {
    TextWithSubViews text = new TextWithSubViews(
      "a[`:´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND,
      text.subView(0).startInclusive
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_BRACKET || punctuation == Punctuation.COLON)
        continue;

      // Invisible to the tokenizer in this context
      if (punctuation == Punctuation.CLOSING_CURLY)
        continue;

      text = new TextWithSubViews(
        "a[:`" + punctuation + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND,
        text.subView(0).startInclusive
      );
    }

    for (InfixOperator operator : InfixOperator.values()) {
      if (operator == InfixOperator.SUBSCRIPTING || operator == InfixOperator.SUBTRACTION || operator == InfixOperator.IN || operator == InfixOperator.MATCHES_REGEX)
        continue;

      text = new TextWithSubViews(
        "a[`:´" + operator
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnExpectingSubstringClosingBracket() {
    TextWithSubViews text = new TextWithSubViews(
      "a[:`test´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET,
      text.subView(0).endExclusive - 1
    );

    text = new TextWithSubViews(
      "a[:`test´ null"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET,
      text.subView(0).endExclusive - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_BRACKET)
        continue;

      // Invisible to the tokenizer in this context
      if (punctuation == Punctuation.CLOSING_CURLY)
        continue;

      text = new TextWithSubViews(
        "a[:test`" + punctuation + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnExpectingSubscriptClosingBracket() {
    TextWithSubViews text = new TextWithSubViews(
      "a[`test´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET,
      text.subView(0).endExclusive - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_BRACKET || punctuation == Punctuation.COLON)
        continue;

      // Invisible to the tokenizer in this context
      if (punctuation == Punctuation.CLOSING_CURLY)
        continue;

      text = new TextWithSubViews(
        "a[test`" + punctuation + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMissingBranchingFalseBranch() {
    TextWithSubViews text = new TextWithSubViews(
      "a then test `else´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_FALSE_BRANCH,
      text.subView(0).endExclusive - 1
    );
  }

  @Test
  public void shouldThrowOnMissingPrefixOperand() {
    for (PrefixOperator operator : PrefixOperator.values()) {
      if (operator.flags.contains(OperatorFlag.NAMED) && operator.flags.contains(OperatorFlag.PARENS)) {
        TextWithSubViews text = new TextWithSubViews(operator + "`(´");

        makeErrorCase(
          text,
          ExpressionParserError.EXPECTED_PREFIX_OPERAND,
          text.subView(0).startInclusive
        );

        continue;
      }

      TextWithSubViews text = new TextWithSubViews("`" + operator + "´");

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_PREFIX_OPERAND,
        text.subView(0).endExclusive - 1
      );
    }
  }

  @Test
  public void shouldThrowOnMissingPrefixOperatorClosingParenthesis() {
    for (PrefixOperator operator : PrefixOperator.values()) {
      if (!operator.flags.contains(OperatorFlag.NAMED) || !operator.flags.contains(OperatorFlag.PARENS))
        continue;

      TextWithSubViews text = new TextWithSubViews(
        operator + "(`my_variable´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_PREFIX_OPERAND_CLOSING_PARENTHESIS,
        text.subView(0).endExclusive - 1
      );
    }
  }

  @Test
  public void shouldThrowOnMissingArrayItem() {
    TextWithSubViews text = new TextWithSubViews(
      "[0`,´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_ITEM,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMissingArrayClosingBracket() {
    TextWithSubViews text = new TextWithSubViews(
      "`[´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews(
      "`[´true"
    );

    makeErrorCase(
      text,
      ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews(
      "`[´true, false"
    );

    makeErrorCase(
      text,
      ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET,
      text.subView(0).startInclusive
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.COMMA || punctuation == Punctuation.CLOSING_BRACKET)
        continue;

      // Invisible to the tokenizer in this context
      if (punctuation == Punctuation.CLOSING_CURLY)
        continue;

      text = new TextWithSubViews(
        "`[´true" + punctuation
      );

      makeErrorCase(
        text,
        ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMissingParenthesesContent() {
    TextWithSubViews text = new TextWithSubViews(
      "`(´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_PARENTHESES_CONTENT,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMissingParenthesesTermination() {
    TextWithSubViews text = new TextWithSubViews(
      "`(´true"
    );

    makeErrorCase(
      text,
      ExpressionParserError.MISSING_CLOSING_PARENTHESIS,
      text.subView(0).startInclusive
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_PARENTHESIS)
        continue;

      // Invisible to the tokenizer in this context
      if (punctuation == Punctuation.CLOSING_CURLY)
        continue;

      text = new TextWithSubViews(
        "`(´true"
      );

      makeErrorCase(
        text,
        ExpressionParserError.MISSING_CLOSING_PARENTHESIS,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMemberAccessUsingNonIdentifierRhs() {
    String[] nonIdentifiers = { "-d", "'hey'", "true", "false", "null", "[]" };

    for (String nonIdentifier : nonIdentifiers) {
      TextWithSubViews text = new TextWithSubViews(
        "a + a.b.`" + nonIdentifier + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_MEMBER_ACCESS_IDENTIFIER_RHS,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMultipleOperandsToANonVariadicPrefixOperator() {
    TextWithSubViews text = new TextWithSubViews(
      "floor(a`,´ b)"
    );

    makeErrorCase(
      text,
      ExpressionParserError.NON_VARIADIC_PREFIX_OPERATOR,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMissingVariadicPrefixOperatorOperand() {
    TextWithSubViews text = new TextWithSubViews(
      "min(1, 2, 3`,´)"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_VARIADIC_OPERAND,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnExpectedMapKey() {
    TextWithSubViews text = new TextWithSubViews("{a, b`,´}");

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_MAP_KEY,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews("{a, b: c`,´}");

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_MAP_KEY,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnExpectedMapValue() {
    TextWithSubViews text = new TextWithSubViews(
      "{a, b`:´}"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_MAP_VALUE,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnExpectedMapClosingCurly() {
    TextWithSubViews text = new TextWithSubViews("`{´a, b: 555");

    makeErrorCase(
      text,
      ExpressionParserError.MISSING_MAP_CLOSING_CURLY,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews("`{´");

    makeErrorCase(
      text,
      ExpressionParserError.MISSING_MAP_CLOSING_CURLY,
      text.subView(0).startInclusive
    );
  }

  private void makeErrorCase(TextWithSubViews input, ExpressionParserError error, int position) {
    ExpressionParseException thrownException = null;

    try {
      ExpressionParser.parse(StringView.of(input.text),  null);
    } catch (ExpressionParseException exception) {
      thrownException = exception;
    }

    Assertions.assertNotNull(thrownException, "Expected an error to be thrown");

    StringWriter stringWriter = new StringWriter();
    thrownException.printStackTrace(new PrintWriter(stringWriter));

    Assertions.assertEquals(error, thrownException.error, "Mismatched on the error-type");
    Assertions.assertEquals(Jsonifier.jsonify(position), Jsonifier.jsonify(thrownException.position), "Mismatched on the char-index");
  }
}
