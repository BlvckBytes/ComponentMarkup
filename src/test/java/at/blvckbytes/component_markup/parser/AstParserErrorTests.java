package at.blvckbytes.component_markup.parser;

import at.blvckbytes.component_markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.xml.CursorPosition;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.xml.XmlEventParser;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AstParserErrorTests {

  private static final Logger logger = Logger.getAnonymousLogger();
  private static final IExpressionEvaluator expressionEvaluator = new GPEEE(logger);

  @Test
  public void shouldThrowOnUnknownTag() {
    makeErrorCase(
      AstParseError.UNKNOWN_TAG,
      "@<unknown-tag>"
    );
  }

  @Test
  public void shouldThrowOnUnknownAttribute() {
    makeErrorCase(
      AstParseError.UNKNOWN_ATTRIBUTE,
      "<hover-text @unknown=5>"
    );

    makeErrorCase(
      AstParseError.UNKNOWN_ATTRIBUTE,
      "<hover-text @unknown=5.5>"
    );

    makeErrorCase(
      AstParseError.UNKNOWN_ATTRIBUTE,
      "<hover-text @unknown=\"hello\">"
    );

    makeErrorCase(
      AstParseError.UNKNOWN_ATTRIBUTE,
      "<hover-text @unknown=true>"
    );

    makeErrorCase(
      AstParseError.UNKNOWN_ATTRIBUTE,
      "<hover-text @unknown={}>"
    );
  }

  @Test
  public void shouldThrowOnUnknownStructuralAttribute() {
    makeErrorCase(
      AstParseError.UNKNOWN_STRUCTURAL_ATTRIBUTE,
      "<container @*unknown>"
    );
  }

  @Test
  public void shouldThrowOnUnnamedLetBinding() {
    makeErrorCase(
      AstParseError.UNNAMED_LET_BINDING,
      "<container @let-=\"\">"
    );

    makeErrorCase(
      AstParseError.UNNAMED_LET_BINDING,
      "<container @let=\"\">"
    );
  }

  @Test
  public void shouldThrowOnUnnamedForLoop() {
    makeErrorCase(
      AstParseError.UNNAMED_FOR_LOOP,
      "<container @*for=\"\">"
    );

    makeErrorCase(
      AstParseError.UNNAMED_FOR_LOOP,
      "<container @*for-=\"\">"
    );
  }

  @Test
  public void shouldThrowOnBindingInUse() {
    makeErrorCase(
      AstParseError.BINDING_IN_USE,
      "<container let-a=\"one\" @let-a=\"two\">"
    );

    makeErrorCase(
      AstParseError.BINDING_IN_USE,
      "<container *for-a=\"one\" @let-a=\"two\">"
    );

    makeErrorCase(
      AstParseError.BINDING_IN_USE,
      "<container let-a=\"one\" @*for-a=\"two\">"
    );
  }

  @Test
  public void shouldThrowOnMalformedIdentifier() {
    makeErrorCase(
      AstParseError.MALFORMED_IDENTIFIER,
      "<container @let-a-b=\"one\">"
    );

    makeErrorCase(
      AstParseError.MALFORMED_IDENTIFIER,
      "<container @let-0abc=\"one\">"
    );

    makeErrorCase(
      AstParseError.MALFORMED_IDENTIFIER,
      "<container @*for-a-b=\"one\">"
    );

    makeErrorCase(
      AstParseError.MALFORMED_IDENTIFIER,
      "<container @*for-0abc=\"one\">"
    );
  }

  @Test
  public void shouldThrowOnUnbalancedAttributeBrackets() {
    makeErrorCase(
      AstParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container @[hello=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnUnbalancedClosingTag() {
    makeErrorCase(
      AstParseError.UNBALANCED_CLOSING_TAG,
      "<red>hello<green>world</green></red>@</red>"
    );

    makeErrorCase(
      AstParseError.UNBALANCED_CLOSING_TAG,
      "<red>hello@</blue>"
    );

    makeErrorCase(
      AstParseError.UNBALANCED_CLOSING_TAG,
      "Hello@</red>"
    );
  }

  @Test
  public void shouldThrowOnNonStringStructuralAttribute() {
    makeNonStringStructuralAttributeCase("if");
    makeNonStringStructuralAttributeCase("else-if");
    makeNonStringStructuralAttributeCase("for-member");
  }

  private void makeNonStringStructuralAttributeCase(String attribute) {
    makeErrorCase(
      AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=5>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=5.5>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=true>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=false>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "={}>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + ">"
    );
  }

  @Test
  public void shouldThrowOnNonStringExpressionAttribute() {
    makeErrorCase(
      AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=5>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=5.5>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=true>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=false>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]={}>"
    );
  }

  @Test
  public void shouldThrowOnNonStringLetAttribute() {
    makeErrorCase(
      AstParseError.NON_STRING_LET_ATTRIBUTE,
      "<container @let=5>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_LET_ATTRIBUTE,
      "<container @let-my_var=5>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_LET_ATTRIBUTE,
      "<container @let-my_var=5.5>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_LET_ATTRIBUTE,
      "<container @let-my_var=true>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_LET_ATTRIBUTE,
      "<container @let-my_var=false>"
    );

    makeErrorCase(
      AstParseError.NON_STRING_LET_ATTRIBUTE,
      "<container @let-my_var={}>"
    );
  }

  @Test
  public void shouldThrowOnExpectedSubtreeValue() {
    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<hover-text @value=\"hello, world\">"
    );

    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<hover-text @value=5>"
    );

    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<hover-text @value=5.5>"
    );

    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<hover-text @value=true>"
    );

    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<hover-text @value=false>"
    );
  }

  @Test
  public void shouldThrowOnExpectedScalarValue() {
    makeErrorCase(
      AstParseError.EXPECTED_SCALAR_VALUE,
      "<key @key={} />"
    );
  }

  @Test
  public void shouldThrowOnExpectedSelfClosingTag() {
    makeErrorCase(
      AstParseError.EXPECTED_SELF_CLOSING_TAG,
      "<key@></key>"
    );
  }

  @Test
  public void shouldThrowOnExpectedOpenCloseTag() {
    makeErrorCase(
      AstParseError.EXPECTED_OPEN_CLOSE_TAG,
      "<red /@>"
    );
  }

  @Test
  public void shouldThrowOnExpectedStructuralAttributeFlag() {
    makeErrorCase(
      AstParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG,
      "<red @*else=\"\">"
    );
  }

  @Test
  public void shouldThrowOnNonSubtreeForSeparator() {
    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<red *for-member=\"members\" @for-separator=.5>"
    );

    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<red *for-member=\"members\" @for-separator=5>"
    );

    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<red *for-member=\"members\" @for-separator=true>"
    );

    makeErrorCase(
      AstParseError.EXPECTED_SUBTREE_VALUE,
      "<red *for-member=\"members\" @for-separator=\"hello\">"
    );
  }

  @Test
  public void shouldThrowOnNonScalarForReversed() {
    makeErrorCase(
      AstParseError.EXPECTED_SCALAR_VALUE,
      "<red *for-member=\"members\" @for-reversed={}>"
    );
  }

  @Test
  public void shouldThrowOnMultipleNonMultiAttributes() {
    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-separator={} @for-separator={}>"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=true @for-reversed=false>"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=.5 @for-reversed=.2>"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=5 @for-reversed=2>"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=\"hello\" @for-reversed=\"hello\">"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item name={} @name={}>"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item name={} @name={}>"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item amount=1 @amount=1>"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item material=\"first\" @material=\"second\">"
    );

    makeErrorCase(
      AstParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<block-nbt interpret=true @interpret=false>"
    );
  }

  @Test
  public void shouldThrowOnMultipleConditions() {
    String[] conditionals = { "*if=\"true\"", "*else-if=\"true\"", "*else" };

    for (int i = 0; i < conditionals.length; ++i) {
      String firstCondition = conditionals[i];
      for (int j = i + 1; j < conditionals.length; ++j) {
        String secondCondition = conditionals[j];

        makeErrorCase(
          AstParseError.MULTIPLE_CONDITIONS,
          "<red " + firstCondition + " @" + secondCondition + ">"
        );
      }

      makeErrorCase(
        AstParseError.MULTIPLE_CONDITIONS,
        "<red " + firstCondition + " @" + firstCondition + ">"
      );
    }
  }

  @Test
  public void shouldThrowOnMultipleLoops() {
    makeErrorCase(
      AstParseError.MULTIPLE_LOOPS,
      "<red *for-member=\"members\" @*for-user=\"users\">"
    );
  }

  @Test
  public void shouldThrowOnMissingPrecedingIfSibling() {
    makeErrorCase(
      AstParseError.MISSING_PRECEDING_IF_SIBLING,
      "@<red *else-if=\"true\">"
    );

    makeErrorCase(
      AstParseError.MISSING_PRECEDING_IF_SIBLING,
      "@<red *else>"
    );
  }

  @Test
  public void shouldThrowOnMissingAttributeValue() {
    makeErrorCase(
      AstParseError.MISSING_ATTRIBUTE_VALUE,
      "<key @key />"
    );

    makeErrorCase(
      AstParseError.MISSING_ATTRIBUTE_VALUE,
      "<container @[attr]>"
    );

    makeErrorCase(
      AstParseError.MISSING_ATTRIBUTE_VALUE,
      "<hover-text @value>"
    );

    makeErrorCase(
      AstParseError.MISSING_ATTRIBUTE_VALUE,
      "<container @let-my_var>"
    );
  }

  private void makeErrorCase(AstParseError error, String... lines) {
    TextWithAnchors input = new TextWithAnchors(lines);
    Throwable thrownError = null;

    try {
      AstParser parser = new AstParser(BuiltInTagRegistry.get(), expressionEvaluator);
      XmlEventParser.parse(input.text, parser);
    } catch (Throwable e) {
      thrownError = e;
    }

    Assertions.assertNotNull(thrownError, "Expected an error to be thrown, but got none");

    Throwable finalThrownError = thrownError;

    if (!(thrownError instanceof AstParseException)) {
      logger.log(Level.SEVERE, "Expected an ast parse exception, but got " + finalThrownError.getClass(), finalThrownError);
      throw new AssertionError();
    }

    CursorPosition position = input.anchor(0);

    Assertions.assertEquals(position.toString(), ((AstParseException) thrownError).position.toString());
    Assertions.assertEquals(error, ((AstParseException) thrownError).error);
  }
}
