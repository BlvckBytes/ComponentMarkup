package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.util.Jsonifiable;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExpressionParserErrorTests {

  // TODO: Write cases for all error-types and indices

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
