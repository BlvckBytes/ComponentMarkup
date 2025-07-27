package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.util.StringView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionTokenizerTests {

  @Test
  public void shouldTokenizeAllTypes() {
    TextWithAnchors input = new TextWithAnchors(
      "`?´ `(´ `!´ `'hello, world'´ `+´ `:´ `[´ `8192´ `>´ `-´ `&&´ `2.7182´ `>=´ `*´ `||´ `true´",
      "`<´ `/´ `??´ `]´ `false´ `<=´ `%´ `null´ `==´ `^´ `my_variable´ `!=´ `&´ `)´ `..´ `.5´",
      "`~^´ `~_´ `~#´ `~!´ `~-´ `~?´ `~|´ `~<´ `\\@´ `\\@\\@´ `**´"
    );

    makeCase(
      input,
      InfixOperator.BRANCHING,
      Punctuation.OPENING_PARENTHESIS,
      PrefixOperator.NEGATION,
      "'hello, world'",
      InfixOperator.ADDITION,
      Punctuation.COLON,
      InfixOperator.SUBSCRIPTING,
      8192,
      InfixOperator.GREATER_THAN,
      PrefixOperator.FLIP_SIGN,
      InfixOperator.CONJUNCTION,
      2.7182,
      InfixOperator.GREATER_THAN_OR_EQUAL,
      InfixOperator.MULTIPLICATION,
      InfixOperator.DISJUNCTION,
      true,
      InfixOperator.LESS_THAN,
      InfixOperator.DIVISION,
      InfixOperator.FALLBACK,
      Punctuation.CLOSING_BRACKET,
      false,
      InfixOperator.LESS_THAN_OR_EQUAL,
      InfixOperator.MODULO,
      null,
      InfixOperator.EQUAL_TO,
      InfixOperator.EXPONENTIATION,
      "my_variable",
      InfixOperator.NOT_EQUAL_TO,
      InfixOperator.CONCATENATION,
      Punctuation.CLOSING_PARENTHESIS,
      InfixOperator.RANGE,
      DotDouble.of(.5),
      PrefixOperator.UPPER_CASE,
      PrefixOperator.LOWER_CASE,
      PrefixOperator.TITLE_CASE,
      PrefixOperator.TOGGLE_CASE,
      PrefixOperator.SLUGIFY,
      PrefixOperator.ASCIIFY,
      PrefixOperator.TRIM,
      PrefixOperator.REVERSE,
      InfixOperator.EXPLODE,
      InfixOperator.EXPLODE_REGEX,
      InfixOperator.REPEAT
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
      TextWithAnchors text = new TextWithAnchors("`" + identifier + "´");
      makeCase(text, identifier);
    }
  }

  @Test
  public void shouldTokenizeIdentifiersWedgedWithDoubles() {
    TextWithAnchors text = new TextWithAnchors(
      "`.3´`a´`.5´`b´`.4´"
    );

    makeCase(
      text,
      DotDouble.of(.3), "a", DotDouble.of(.5), "b", DotDouble.of(.4)
    );
  }

  @Test
  public void shouldTokenizeRangeWedgedWithDoubles() {
    TextWithAnchors text = new TextWithAnchors(
      "`.5´`..´`.3´"
    );

    makeCase(
      text,
      DotDouble.of(.5), InfixOperator.RANGE, DotDouble.of(.3)
    );
  }

  @Test
  public void shouldTokenizeInfixOperatorsAndPunctuationWedgedWithIdentifiers() {
    List<Object> items = new ArrayList<>();

    items.addAll(Arrays.asList(InfixOperator.values()));
    items.addAll(Arrays.asList(Punctuation.values()));

    for (Object item : items) {
      TextWithAnchors text = new TextWithAnchors(
        "`before´ `a´`" + TextWithAnchors.escape(item) + "´`b´ `after´"
      );

      makeCase(
        text,
        "before", "a", item, "b", "after"
      );
    }
  }

  @Test
  public void shouldTokenizeDashesCorrectly() {
    TextWithAnchors text = new TextWithAnchors("`-´`identifier´");
    makeCase(text, PrefixOperator.FLIP_SIGN, "identifier");

    text = new TextWithAnchors("`-´`5.0´");
    makeCase(text, PrefixOperator.FLIP_SIGN, 5.0);

    text = new TextWithAnchors("`-´`.5´");
    makeCase(text, PrefixOperator.FLIP_SIGN, DotDouble.of(.5));

    text = new TextWithAnchors("`5´ `-´ `-´ `3´");
    makeCase(text, 5, InfixOperator.SUBTRACTION, PrefixOperator.FLIP_SIGN, 3);

    text = new TextWithAnchors("`5´ `-´`-´ `-´ `3´ `-´ `[´");

    makeCase(
      text,
      5,
      InfixOperator.SUBTRACTION,
      PrefixOperator.FLIP_SIGN,
      PrefixOperator.FLIP_SIGN,
      3,
      InfixOperator.SUBTRACTION,
      InfixOperator.SUBSCRIPTING
    );
  }

  @Test
  public void shouldTokenizeRangeOperator() {
    TextWithAnchors text = new TextWithAnchors(
      "`0´`..´`100´"
    );

    makeCase(
      text,
      0, InfixOperator.RANGE, 100
    );

    text = new TextWithAnchors(
      "`a´`..´`b´"
    );

    makeCase(
      text,
      "a", InfixOperator.RANGE, "b"
    );
  }

  @Test
  public void shouldTokenizeValidStrings() {
    TextWithAnchors text = new TextWithAnchors(
      "`'hello, \" world @\\' \\\"'´ `\"double ' quotes \\' @\\\"\"´"
    );

    text.addViewIndexToBeRemoved(text.anchor(0));
    text.addViewIndexToBeRemoved(text.anchor(1));

    makeCase(
      text,
      "'hello, \" world ' \\\"'",
      "\"double ' quotes \\' \"\""
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

    text = new TextWithAnchors(
      "@~"
    );

    makeErrorCase(
      text,
      ExpressionTokenizeError.SINGLE_TILDE
    );
  }

  private static void makeErrorCase(TextWithAnchors input, ExpressionTokenizeError expectedError) {
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
    assertEquals(
      Jsonifier.jsonify(input.anchor(0)),
      Jsonifier.jsonify(thrownException.position),
      "Encountered mismatch on thrown error beginIndex"
    );
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

        token = new StringToken(subView, stringContents);
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

  private static void makeCase(TextWithAnchors input, Object... expectedValues) {
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

    for (int valueIndex = 0; valueIndex < expectedValues.length; ++valueIndex) {
      Object expectedValue = expectedValues[valueIndex];

      if (expectedTokensString.length() != 0)
        expectedTokensString.append('\n');

      expectedTokensString.append(Jsonifier.jsonify(makeToken(expectedValue, input.subView(valueIndex))));
    }

    assertEquals(expectedTokensString.toString(), actualTokensString.toString());
  }
}
