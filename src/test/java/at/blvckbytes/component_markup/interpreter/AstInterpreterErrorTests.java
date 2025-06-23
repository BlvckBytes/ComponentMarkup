package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.parser.AstParseError;
import at.blvckbytes.component_markup.parser.AstParseException;
import at.blvckbytes.component_markup.parser.AstParser;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.xml.XmlEventParser;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class AstInterpreterErrorTests {

  private static final IExpressionEvaluator expressionEvaluator = new GPEEE(Logger.getAnonymousLogger());

  @Test
  public void shouldThrowOnMultipleNonMultiAttributes() {
    TextWithAnchors text = new TextWithAnchors(
      "<hover-item material=\"first\" @material=\"second\">"
    );

    makeErrorCase(
      text,
      text.anchor(0),
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE
    );
  }

  private void makeErrorCase(TextWithAnchors input, CursorPosition position, AstParseError error) {
    Throwable thrownError = null;

    try {
      AstParser parser = new AstParser(BuiltInTagRegistry.get(), expressionEvaluator);
      XmlEventParser.parse(input.text, parser);
    } catch (Throwable e) {
      thrownError = e;
    }

    Assertions.assertNotNull(thrownError, "Expected an error to be thrown, but got none");
    Assertions.assertInstanceOf(AstParseException.class, thrownError, "Expected an ast parse exception, but got " + thrownError.getClass());

    Assertions.assertEquals(position.toString(), ((AstParseException) thrownError).position.toString());
    Assertions.assertEquals(error, ((AstParseException) thrownError).error);
  }
}
