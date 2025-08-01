/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
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
      TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "a[0][1`,´ 2]"
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
      TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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

      text = new TextWithAnchors(
        "a[:`" + punctuation + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND,
        text.subView(0).startInclusive
      );
    }

    for (InfixOperator operator : InfixOperator.values()) {
      if (operator == InfixOperator.SUBSCRIPTING || operator == InfixOperator.SUBTRACTION || operator == InfixOperator.CONTAINS || operator == InfixOperator.MATCHES_REGEX)
        continue;

      text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "a[:`test´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET,
      text.subView(0).endExclusive - 1
    );

    text = new TextWithAnchors(
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

      text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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

      text = new TextWithAnchors(
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
  public void shouldThrowOnMissingBranchingDelimiter() {
    TextWithAnchors text = new TextWithAnchors(
      "a ? `test´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_BRANCH_DELIMITER,
      text.subView(0).endExclusive - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.COLON)
        continue;

      text = new TextWithAnchors(
        "a ? test`" + punctuation + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_BRANCH_DELIMITER,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMissingBranchingFalseBranch() {
    TextWithAnchors text = new TextWithAnchors(
      "a ? test `:´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_FALSE_BRANCH,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMissingPrefixOperand() {
    for (PrefixOperator operator : PrefixOperator.values()) {
      TextWithAnchors text = new TextWithAnchors(
        "`" + operator + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_PREFIX_OPERAND,
        text.subView(0).endExclusive - 1
      );
    }
  }

  @Test
  public void shouldThrowOnMissingArrayItem() {
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "`[´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
      text.subView(0).startInclusive
    );

    text = new TextWithAnchors(
      "[`true´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
      text.subView(0).endExclusive - 1
    );

    text = new TextWithAnchors(
      "[true, `false´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
      text.subView(0).endExclusive - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.COMMA || punctuation == Punctuation.CLOSING_BRACKET)
        continue;

      text = new TextWithAnchors(
        "[true`" + punctuation + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMissingParenthesesContent() {
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "(`true´"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS,
      text.subView(0).endExclusive - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_PARENTHESIS)
        continue;

      text = new TextWithAnchors(
        "(true`" + punctuation + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMemberAccessUsingNonIdentifierRhs() {
    String[] nonIdentifiers = { "-d", "'hey'", "true", "false", "null", "[]" };

    for (String nonIdentifier : nonIdentifiers) {
      TextWithAnchors text = new TextWithAnchors(
        "a + a.b.`" + nonIdentifier + "´"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_MEMBER_ACCESS_IDENTIFIER_RHS,
        text.subView(0).startInclusive
      );
    }
  }

  private void makeErrorCase(TextWithAnchors input, ExpressionParserError error, int position) {
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
