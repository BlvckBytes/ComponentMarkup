package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionTokenizerTests {

  @Test
  public void shouldTokenizeAllTypes() {
    TextWithAnchors input = new TextWithAnchors(
      "@? @( @! @'hello, world' @+ @: @[ @8192 @> @- @&& @2.7182 @>= @* @|| @true",
      "@< @/ @?? @] @false @<= @% @null @== @^ @my_variable @!= @& @) @.. @.5"
    );

    makeCase(
      input,
      Operator.TERNARY_BEGIN,
      Punctuation.OPENING_PARENTHESIS,
      Operator.NEGATION,
      "'hello, world'",
      Operator.ADDITION,
      Operator.TERNARY_DELIMITER,
      Punctuation.OPENING_BRACKET,
      8192,
      Operator.GREATER_THAN,
      Operator.SUBTRACTION,
      Operator.CONJUNCTION,
      2.7182,
      Operator.GREATER_THAN_OR_EQUAL,
      Operator.MULTIPLICATION,
      Operator.DISJUNCTION,
      true,
      Operator.LESS_THAN,
      Operator.DIVISION,
      Operator.NULL_COALESCE,
      Punctuation.CLOSING_BRACKET,
      false,
      Operator.LESS_THAN_OR_EQUAL,
      Operator.MODULO,
      null,
      Operator.EQUAL_TO,
      Operator.EXPONENTIATION,
      "my_variable",
      Operator.NOT_EQUAL_TO,
      Operator.CONCATENATION,
      Punctuation.CLOSING_PARENTHESIS,
      Operator.RANGE,
      .5
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
      TextWithAnchors text = new TextWithAnchors("@" + identifier);
      makeCase(text, identifier);
    }
  }

  @Test
  public void shouldTokenizeIdentifiersWedgedWithDoubles() {
    TextWithAnchors text = new TextWithAnchors(
      "@.3@a@.5@b@.4"
    );

    makeCase(
      text,
      .3, "a", .5, "b", .4
    );
  }

  @Test
  public void shouldTokenizeRangeWedgedWithDoubles() {
    TextWithAnchors text = new TextWithAnchors(
      "@.5@..@.3"
    );

    makeCase(
      text,
      .5, Operator.RANGE, .3
    );
  }

  @Test
  public void shouldTokenizeIdentifiersWedgedWithOperatorsAndPunctuation() {
    List<Object> nonIdentifiers = new ArrayList<>();

    nonIdentifiers.addAll(Arrays.asList(Operator.values()));
    nonIdentifiers.addAll(Arrays.asList(Punctuation.values()));

    for (Object nonIdentifier : nonIdentifiers) {
      TextWithAnchors text = new TextWithAnchors(
        "@before @a@" + nonIdentifier + "@b @after"
      );

      makeCase(
        text,
        "before", "a", nonIdentifier, "b", "after"
      );
    }
  }

  @Test
  public void shouldTokenizeRangeOperator() {
    TextWithAnchors text = new TextWithAnchors(
      "@0@..@100"
    );

    makeCase(
      text,
      0, Operator.RANGE, 100
    );

    text = new TextWithAnchors(
      "@a@..@b"
    );

    makeCase(
      text,
      "a", Operator.RANGE, "b"
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
      TextWithAnchors text = new TextWithAnchors(
        "@" + malformedIdentifier
      );

      makeErrorCase(
        text,
        ExpressionTokenizeError.MALFORMED_IDENTIFIER
      );
    }
  }

  @Test
  public void shouldThrowOnUnterminatedString() {
    TextWithAnchors text = new TextWithAnchors(
      "@'my string value"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.UNTERMINATED_STRING
    );

    text = new TextWithAnchors(
      "@'my string \n value'"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.UNTERMINATED_STRING
    );
  }

  @Test
  public void shouldThrowOnMalformedDecimal() {
    TextWithAnchors text = new TextWithAnchors(
      "@12."
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS
    );
  }

  @Test
  public void shouldThrowOnMalformedKnownOperators() {
    TextWithAnchors text = new TextWithAnchors(
      "@|"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.SINGLE_PIPE
    );

    text = new TextWithAnchors(
      "@="
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.SINGLE_EQUALS
    );
  }

  private static void makeErrorCase(TextWithAnchors input, ExpressionTokenizeError expectedError) {
    ExpressionTokenizeException thrownException = null;

    try {
      ExpressionTokenizer tokenizer = new ExpressionTokenizer(input.text);

      while (tokenizer.peekToken() != null)
        tokenizer.nextToken();
    } catch (ExpressionTokenizeException exception) {
      thrownException = exception;
    }

    if (thrownException == null)
      throw new IllegalStateException("Expected there to be an error of " + expectedError + ", but encountered none");

    assertEquals(expectedError, thrownException.error, "Encountered mismatch on thrown error-types");
    assertEquals(input.anchorIndex(0), thrownException.beginIndex, "Encountered mismatch on thrown error beginIndex");
  }

  private static void makeCase(TextWithAnchors input, Object... expectedValues) {
    StringBuilder actualTokensString = new StringBuilder();

    ExpressionTokenizer tokenizer = new ExpressionTokenizer(input.text);

    while (tokenizer.peekToken() != null) {
      if (actualTokensString.length() != 0)
        actualTokensString.append('\n');

      actualTokensString.append(tokenizer.nextToken());
    }

    StringBuilder expectedTokensString = new StringBuilder();

    for (int valueIndex = 0; valueIndex < expectedValues.length; ++valueIndex) {
      Object expectedValue = expectedValues[valueIndex];

      if (expectedTokensString.length() != 0)
        expectedTokensString.append('\n');

      int charIndex = input.anchorIndex(valueIndex);

      ExpressionToken token;

      if (expectedValue instanceof Boolean)
        token = new BooleanToken(charIndex, (boolean) expectedValue);
      else if (expectedValue instanceof Double || expectedValue instanceof Float)
        token = new DoubleToken(charIndex, ((Number) expectedValue).doubleValue());
      else if (expectedValue instanceof Integer || expectedValue instanceof Long)
        token = new LongToken(charIndex, ((Number) expectedValue).longValue());
      else if (expectedValue instanceof Operator)
        token = new OperatorToken(charIndex, (Operator) expectedValue);
      else if (expectedValue instanceof Punctuation)
        token = new PunctuationToken(charIndex, (Punctuation) expectedValue);
      else if (expectedValue instanceof String) {
        String stringValue = (String) expectedValue;
        int stringLength = stringValue.length();

        if (stringLength == 0)
          throw new IllegalStateException("Invalid empty string at index " + valueIndex);

        if (stringValue.charAt(0) == '\'') {
          if (stringValue.charAt(stringLength - 1) != '\'')
            throw new IllegalStateException("Invalid string: " + stringValue);

          token = new StringToken(charIndex, stringValue.substring(1, stringLength - 1));
        }
        else
          token = new IdentifierToken(charIndex, stringValue);
      }
      else if (expectedValue == null)
        token = new NullToken(charIndex);

      else
        throw new IllegalStateException("Invalid value: " + expectedValue);

      expectedTokensString.append(token);
    }

    assertEquals(expectedTokensString.toString(), actualTokensString.toString());
  }
}
