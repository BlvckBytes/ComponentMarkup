package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
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
        "a + b @" + trailingToken
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_EOS,
        text.anchorIndex(0)
      );
    }
  }

  @Test
  public void shouldThrowOnArrayAfterSubscripting() {
    TextWithAnchors text = new TextWithAnchors(
      "a[0][1@, 2]"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET,
      text.anchorIndex(0)
    );
  }

  @Test
  public void shouldThrowOnMissingInfixRightOperand() {
    for (InfixOperator operator : InfixOperator.values()) {
      TextWithAnchors text = new TextWithAnchors(
        "a " + TextWithAnchors.escape(operator) + "@"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_RIGHT_INFIX_OPERAND,
        text.anchorIndex(0) - 1
      );
    }
  }

  @Test
  public void shouldThrowOnExpectingSubstringUpperBound() {
    TextWithAnchors text = new TextWithAnchors(
      "a[@:"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND,
      text.anchorIndex(0)
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_BRACKET)
        continue;

      text = new TextWithAnchors(
        "a[:@" + TextWithAnchors.escape(punctuation)
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND,
        text.anchorIndex(0)
      );
    }

    for (InfixOperator operator : InfixOperator.values()) {
      if (operator == InfixOperator.SUBSCRIPTING || operator == InfixOperator.SUBTRACTION)
        continue;

      text = new TextWithAnchors(
        "a[@:" + TextWithAnchors.escape(operator)
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND,
        text.anchorIndex(0)
      );
    }
  }

  @Test
  public void shouldThrowOnExpectingSubstringClosingBracket() {
    TextWithAnchors text = new TextWithAnchors(
      "a[:test@"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET,
      text.anchorIndex(0) - 1
    );

    text = new TextWithAnchors(
      "a[:test@ null"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET,
      text.anchorIndex(0) - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_BRACKET)
        continue;

      text = new TextWithAnchors(
        "a[:test@" + TextWithAnchors.escape(punctuation)
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET,
        text.anchorIndex(0)
      );
    }
  }

  @Test
  public void shouldThrowOnExpectingSubscriptClosingBracket() {
    TextWithAnchors text = new TextWithAnchors(
      "a[test@"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET,
      text.anchorIndex(0) - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_BRACKET || punctuation == Punctuation.COLON)
        continue;

      text = new TextWithAnchors(
        "a[test@" + TextWithAnchors.escape(punctuation)
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET,
        text.anchorIndex(0)
      );
    }
  }

  @Test
  public void shouldThrowOnMissingBranchingDelimiter() {
    TextWithAnchors text = new TextWithAnchors(
      "a ? test@"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_BRANCH_DELIMITER,
      text.anchorIndex(0) - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.COLON)
        continue;

      text = new TextWithAnchors(
        "a ? test@" + TextWithAnchors.escape(punctuation)
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_BRANCH_DELIMITER,
        text.anchorIndex(0)
      );
    }
  }

  @Test
  public void shouldThrowOnMissingBranchingFalseBranch() {
    TextWithAnchors text = new TextWithAnchors(
      "a ? test @:"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_FALSE_BRANCH,
      text.anchorIndex(0)
    );
  }

  @Test
  public void shouldThrowOnMissingPrefixOperand() {
    for (PrefixOperator operator : PrefixOperator.values()) {
      TextWithAnchors text = new TextWithAnchors(
        TextWithAnchors.escape(operator) + "@"
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_PREFIX_OPERAND,
        text.anchorIndex(0) - 1
      );
    }
  }

  @Test
  public void shouldThrowOnMissingArrayItem() {
    TextWithAnchors text = new TextWithAnchors(
      "[0@,"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_ITEM,
      text.anchorIndex(0)
    );
  }

  @Test
  public void shouldThrowOnMissingArrayClosingBracket() {
    TextWithAnchors text = new TextWithAnchors(
      "@["
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
      text.anchorIndex(0)
    );

    text = new TextWithAnchors(
      "[true@"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
      text.anchorIndex(0) - 1
    );

    text = new TextWithAnchors(
      "[true, false@"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
      text.anchorIndex(0) - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.COMMA || punctuation == Punctuation.CLOSING_BRACKET)
        continue;

      text = new TextWithAnchors(
        "[true@" + TextWithAnchors.escape(punctuation)
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET,
        text.anchorIndex(0)
      );
    }
  }

  @Test
  public void shouldThrowOnMissingParenthesesContent() {
    TextWithAnchors text = new TextWithAnchors(
      "@("
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_PARENTHESES_CONTENT,
      text.anchorIndex(0)
    );
  }

  @Test
  public void shouldThrowOnMissingParenthesesTermination() {
    TextWithAnchors text = new TextWithAnchors(
      "(true@"
    );

    makeErrorCase(
      text,
      ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS,
      text.anchorIndex(0) - 1
    );

    for (Punctuation punctuation : Punctuation.values()) {
      if (punctuation == Punctuation.CLOSING_PARENTHESIS)
        continue;

      text = new TextWithAnchors(
        "(true@" + TextWithAnchors.escape(punctuation)
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS,
        text.anchorIndex(0)
      );
    }
  }

  @Test
  public void shouldThrowOnMemberAccessUsingNonIdentifierRhs() {
    String[] nonIdentifiers = { "-d", "'hey'", "true", "false", "null", "[]" };

    for (String nonIdentifier : nonIdentifiers) {
      TextWithAnchors text = new TextWithAnchors(
        "a + a.b.@" + nonIdentifier
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_MEMBER_ACCESS_IDENTIFIER_RHS,
        text.anchorIndex(0)
      );
    }
  }

  private void makeErrorCase(TextWithAnchors input, ExpressionParserError error, int charIndex) {
    ExpressionParserException thrownException = null;

    try {
      ExpressionParser.parse(input.text);
    } catch (ExpressionParserException exception) {
      thrownException = exception;
    }

    Assertions.assertNotNull(thrownException, "Expected an error to be thrown");

    StringWriter stringWriter = new StringWriter();
    thrownException.printStackTrace(new PrintWriter(stringWriter));

    ExpressionParserException expectedException = new ExpressionParserException(error, charIndex);

    Assertions.assertEquals(Jsonifiable.toString(expectedException), Jsonifiable.toString(thrownException), () -> (
      "Mismatched on the exception thrown:\n" + stringWriter
    ));
  }
}
