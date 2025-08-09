/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.util.StringView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionTokenizerTests {

  @Test
  public void shouldTokenizeAllTypes() {
    TextWithSubViews text = new TextWithSubViews(
      "`?´ `(´ `!´ `'hello, world'´ `+´ `:´ `[´ `8192´ `>´ `-´ `&&´ `2.7182´ `>=´ `*´ `||´ `true´",
      "`<´ `/´ `??´ `]´ `false´ `<=´ `%´ `null´ `==´ `^´ `my_variable´ `!=´ `&´ `)´ `..´ `.5´",
      "`~^´ `~_´ `~#´ `~!´ `~-´ `~?´ `~|´ `~<´ `@´ `@@´ `**´ `::´ `:::´ `:::´`::´"
    );

    int index = 0;

    makeCase(
      text,
      InfixOperator.BRANCHING, text.subView(index++),
      Punctuation.OPENING_PARENTHESIS, text.subView(index++),
      PrefixOperator.NEGATION, text.subView(index++),
      "'hello, world'", text.subView(index++),
      InfixOperator.ADDITION, text.subView(index++),
      Punctuation.COLON, text.subView(index++),
      InfixOperator.SUBSCRIPTING, text.subView(index++),
      8192, text.subView(index++),
      InfixOperator.GREATER_THAN, text.subView(index++),
      PrefixOperator.FLIP_SIGN, text.subView(index++),
      InfixOperator.CONJUNCTION, text.subView(index++),
      2.7182, text.subView(index++),
      InfixOperator.GREATER_THAN_OR_EQUAL, text.subView(index++),
      InfixOperator.MULTIPLICATION, text.subView(index++),
      InfixOperator.DISJUNCTION, text.subView(index++),
      true, text.subView(index++),
      InfixOperator.LESS_THAN, text.subView(index++),
      InfixOperator.DIVISION, text.subView(index++),
      InfixOperator.FALLBACK, text.subView(index++),
      Punctuation.CLOSING_BRACKET, text.subView(index++),
      false, text.subView(index++),
      InfixOperator.LESS_THAN_OR_EQUAL, text.subView(index++),
      InfixOperator.MODULO, text.subView(index++),
      null, text.subView(index++),
      InfixOperator.EQUAL_TO, text.subView(index++),
      InfixOperator.EXPONENTIATION, text.subView(index++),
      "my_variable", text.subView(index++),
      InfixOperator.NOT_EQUAL_TO, text.subView(index++),
      InfixOperator.CONCATENATION, text.subView(index++),
      Punctuation.CLOSING_PARENTHESIS, text.subView(index++),
      InfixOperator.RANGE, text.subView(index++),
      DotDouble.of(.5), text.subView(index++),
      PrefixOperator.UPPER_CASE, text.subView(index++),
      PrefixOperator.LOWER_CASE, text.subView(index++),
      PrefixOperator.TITLE_CASE, text.subView(index++),
      PrefixOperator.TOGGLE_CASE, text.subView(index++),
      PrefixOperator.SLUGIFY, text.subView(index++),
      PrefixOperator.ASCIIFY, text.subView(index++),
      PrefixOperator.TRIM, text.subView(index++),
      PrefixOperator.REVERSE, text.subView(index++),
      InfixOperator.EXPLODE, text.subView(index++),
      InfixOperator.EXPLODE_REGEX, text.subView(index++),
      InfixOperator.REPEAT, text.subView(index++),
      InfixOperator.CONTAINS, text.subView(index++),
      InfixOperator.MATCHES_REGEX, text.subView(index++),
      InfixOperator.MATCHES_REGEX, text.subView(index++),
      InfixOperator.CONTAINS, text.subView(index)
    );
  }

  @Test
  public void shouldTokenizeMisleadingIdentifiers() {
    String[] identifiers = {
      "truea",
      "falsea",
      "nulla",
      "atrue",
      "afalse",
      "anull",
      "atrueb",
      "afalseb",
      "anullb",
    };

    for (String identifier : identifiers) {
      TextWithSubViews text = new TextWithSubViews("`" + identifier + "´");

      makeCase(
        text,
        identifier, text.subView(0)
      );
    }
  }

  @Test
  public void shouldTokenizeIdentifiersWedgedWithDoubles() {
    TextWithSubViews text = new TextWithSubViews(
      "`.3´`a´`.5´`b´`.4´"
    );

    makeCase(
      text,
      DotDouble.of(.3), text.subView(0),
      "a", text.subView(1),
      DotDouble.of(.5), text.subView(2),
      "b", text.subView(3),
      DotDouble.of(.4), text.subView(4)
    );
  }

  @Test
  public void shouldTokenizeRangeWedgedWithDoubles() {
    TextWithSubViews text = new TextWithSubViews(
      "`.5´`..´`.3´"
    );

    makeCase(
      text,
      DotDouble.of(.5), text.subView(0),
      InfixOperator.RANGE, text.subView(1),
      DotDouble.of(.3), text.subView(2)
    );
  }

  @Test
  public void shouldTokenizeInfixOperatorsAndPunctuationWedgedWithIdentifiers() {
    List<Object> items = new ArrayList<>();

    items.addAll(Arrays.asList(InfixOperator.values()));
    items.addAll(Arrays.asList(Punctuation.values()));

    for (Object item : items) {
      TextWithSubViews text = new TextWithSubViews(
        "`before´ `a´`" + item + "´`b´ `after´"
      );

      makeCase(
        text,
        "before", text.subView(0),
        "a", text.subView(1),
        item, text.subView(2),
        "b", text.subView(3),
        "after", text.subView(4)
      );
    }
  }

  @Test
  public void shouldTokenizeDashesCorrectly() {
    TextWithSubViews text = new TextWithSubViews("`-´`identifier´");

    makeCase(
      text,
      PrefixOperator.FLIP_SIGN, text.subView(0),
      "identifier", text.subView(1)
    );

    text = new TextWithSubViews("`-´`5.0´");

    makeCase(
      text,
      PrefixOperator.FLIP_SIGN, text.subView(0),
      5.0, text.subView(1)
    );

    text = new TextWithSubViews("`-´`.5´");

    makeCase(
      text,
      PrefixOperator.FLIP_SIGN, text.subView(0),
      DotDouble.of(.5), text.subView(1)
    );

    text = new TextWithSubViews("`5´ `-´ `-´ `3´");

    makeCase(
      text,
      5, text.subView(0),
      InfixOperator.SUBTRACTION, text.subView(1),
      PrefixOperator.FLIP_SIGN, text.subView(2),
      3, text.subView(3)
    );

    text = new TextWithSubViews("`5´ `-´`-´ `-´ `3´ `-´ `[´");

    makeCase(
      text,
      5, text.subView(0),
      InfixOperator.SUBTRACTION, text.subView(1),
      PrefixOperator.FLIP_SIGN, text.subView(2),
      PrefixOperator.FLIP_SIGN, text.subView(3),
      3, text.subView(4),
      InfixOperator.SUBTRACTION, text.subView(5),
      InfixOperator.SUBSCRIPTING, text.subView(6)
    );
  }

  @Test
  public void shouldTokenizeRangeOperator() {
    TextWithSubViews text = new TextWithSubViews(
      "`0´`..´`100´"
    );

    makeCase(
      text,
      0, text.subView(0),
      InfixOperator.RANGE, text.subView(1),
      100, text.subView(2)
    );

    text = new TextWithSubViews(
      "`a´`..´`b´"
    );

    makeCase(
      text,
      "a", text.subView(0),
      InfixOperator.RANGE, text.subView(1),
      "b", text.subView(2)
    );
  }

  @Test
  public void shouldTokenizeValidStrings() {
    TextWithSubViews text = new TextWithSubViews(
      "`'hello, \" world `\\´' \\\"'´ `\"double ' quotes \\' `\\´\"\"´"
    );

    text.addViewIndexToBeRemoved(text.subView(1).startInclusive);
    text.addViewIndexToBeRemoved(text.subView(3).startInclusive);

    makeCase(
      text,
      "'hello, \" world ' \\\"'", text.subView(0),
      "\"double ' quotes \\' \"\"", text.subView(2)
    );
  }

  @Test
  public void shouldThrowOnMalformedIdentifier() {
    String[] malformedIdentifiers = {
      "my_IDEntifier",
      "öabc",
      "$test",
      "te§t",
      "_leading_underscore",
      "double__underscore"
    };

    for (String malformedIdentifier : malformedIdentifiers) {
      TextWithSubViews text = new TextWithSubViews(
        "`" + malformedIdentifier + "´"
      );

      makeErrorCase(
        text,
        ExpressionTokenizeError.MALFORMED_IDENTIFIER,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnUnterminatedString() {
    TextWithSubViews text = new TextWithSubViews(
      "`'´my string value"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.UNTERMINATED_STRING,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews(
      "`'´my string \n value'"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.UNTERMINATED_STRING,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMalformedDecimal() {
    TextWithSubViews text = new TextWithSubViews(
      "`12.´"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMalformedKnownOperators() {
    TextWithSubViews text = new TextWithSubViews(
      "`|´"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.SINGLE_PIPE,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews(
      "`=´"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.SINGLE_EQUALS,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews(
      "`~´"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.SINGLE_TILDE,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnUnterminatedInterpolation() {
    TextWithSubViews text = new TextWithSubViews(
      "×`hello `{´ a + b world ×`"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.UNTERMINATED_TEMPLATE_LITERAL_INTERPOLATION,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews(
      "×`hello `{´ a + b { world ×`"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.UNTERMINATED_TEMPLATE_LITERAL_INTERPOLATION,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnUnescapedCurlyWithinTemplateLiteral() {
    TextWithSubViews text = new TextWithSubViews(
      "×`hello `}´ world ×`"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.UNESCAPED_TEMPLATE_LITERAL_CURLY,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnEmptyPlaceholderWithinTemplateLiteral() {
    TextWithSubViews text = new TextWithSubViews("×`hello `{}´ world ×`");

    makeErrorCase(
      text,
      ExpressionTokenizeError.EMPTY_TEMPLATE_LITERAL_INTERPOLATION,
      text.subView(0).startInclusive
    );

    text = new TextWithSubViews("×`hello `{   }´ world ×`");

    makeErrorCase(
      text,
      ExpressionTokenizeError.EMPTY_TEMPLATE_LITERAL_INTERPOLATION,
      text.subView(0).startInclusive
    );
  }

  private static void makeErrorCase(TextWithSubViews input, ExpressionTokenizeError expectedError, int expectedPosition) {
    ExpressionTokenizeException thrownException = null;

    try {
      ExpressionTokenizer tokenizer = new ExpressionTokenizer(StringView.of(input.text), null);

      while (tokenizer.peekToken() != null)
        tokenizer.nextToken();
    } catch (ExpressionTokenizeException exception) {
      thrownException = exception;
    }

    if (thrownException == null)
      throw new IllegalStateException("Expected there to be an error of " + expectedError + ", but encountered none");

    assertEquals(expectedError, thrownException.error, "Encountered mismatch on thrown error-types");
    assertEquals(expectedPosition, thrownException.position, "Encountered mismatch on thrown error beginIndex");
  }

  public static Token makeToken(Object value, StringView subView) {
    Token token;

    if (value instanceof Boolean)
      token = new BooleanToken(subView, (boolean) value);
    else if (value instanceof DotDouble) {
      DotDouble dotDouble = (DotDouble) value;
      token = new DoubleToken(subView, dotDouble.value);
    } else if (value instanceof Double || value instanceof Float) {
      double number = ((Number) value).doubleValue();
      token = new DoubleToken(subView, number);
    } else if (value instanceof Integer || value instanceof Long) {
      long number = ((Number) value).longValue();
      token = new LongToken(subView, number);
    } else if (value instanceof InfixOperator)
      token = new InfixOperatorToken(subView, (InfixOperator) value);
    else if (value instanceof PrefixOperator)
      token = new PrefixOperatorToken(subView, (PrefixOperator) value);
    else if (value instanceof Punctuation)
      token = new PunctuationToken(subView, (Punctuation) value);
    else if (value instanceof String) {
      String stringValue = (String) value;
      int stringLength = stringValue.length();

      if (stringLength == 0)
        throw new IllegalStateException("An empty string cannot represent a token");

      char quoteChar;

      if ((quoteChar = stringValue.charAt(0)) == '\'' || quoteChar == '"') {
        if (stringValue.charAt(stringLength - 1) != quoteChar)
          throw new IllegalStateException("Invalid string: " + stringValue);

        String stringContents = stringValue.substring(1, stringLength - 1);
        StringView contentsView = subView.buildSubViewRelative(1, -1);

        // Make sure the provided plain-string equals the desired value.
        // It's really only used as a sentinel to identify strings,
        // but since it also improves case-readability, so I'll keep it this way.
        Assertions.assertEquals(stringContents, contentsView.buildString());

        token = new StringToken(subView, contentsView);
      }
      else
        token = new IdentifierToken(subView, stringValue);
    }
    else if (value == null)
      token = new NullToken(subView);

    else
      throw new IllegalStateException("Invalid token-representing value: " + value);

    return token;
  }

  private static void makeCase(TextWithSubViews input, Object... expectedValuesAndViews) {
    List<Token> actualTokens = new ArrayList<>();
    ExpressionTokenizer tokenizer = new ExpressionTokenizer(StringView.of(input.text), null);

    while (tokenizer.peekToken() != null)
      actualTokens.add(tokenizer.nextToken());

    StringBuilder actualTokensString = new StringBuilder();

    for (Token actualToken : actualTokens) {
      if (actualTokensString.length() != 0)
        actualTokensString.append('\n');

      actualTokensString.append(Jsonifier.jsonify(actualToken));
    }

    StringBuilder expectedTokensString = new StringBuilder();

    if (expectedValuesAndViews.length % 2 != 0)
      throw new IllegalStateException("Expected there to be a multiple of two of variadic arguments - pairs of values and views");

    for (int valueIndex = 0; valueIndex < expectedValuesAndViews.length; valueIndex += 2) {
      Object expectedValue = expectedValuesAndViews[valueIndex];
      StringView expectedView = (StringView) expectedValuesAndViews[valueIndex + 1];

      if (expectedTokensString.length() != 0)
        expectedTokensString.append('\n');

      expectedTokensString.append(Jsonifier.jsonify(makeToken(expectedValue, expectedView)));
    }

    assertEquals(expectedTokensString.toString(), actualTokensString.toString());
  }
}
