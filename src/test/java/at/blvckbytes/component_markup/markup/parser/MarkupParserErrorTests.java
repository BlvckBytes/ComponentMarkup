package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.Jsonifier;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MarkupParserErrorTests {

  @Test
  public void shouldThrowOnUnknownTag() {
    makeErrorCase(
      MarkupParseError.UNKNOWN_TAG,
      "@<unknown-tag>"
    );
  }

  @Test
  public void shouldThrowOnUnknownAttribute() {
    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} @unknown=5>"
    );

    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} @unknown=5.5>"
    );

    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} @unknown=\"hello\">"
    );

    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} @unknown=true>"
    );

    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} @unknown={}>"
    );
  }

  @Test
  public void shouldThrowOnUnknownStructuralAttribute() {
    makeErrorCase(
      MarkupParseError.UNKNOWN_STRUCTURAL_ATTRIBUTE,
      "<container @*unknown>"
    );
  }

  @Test
  public void shouldThrowOnUnnamedLetBinding() {
    makeErrorCase(
      MarkupParseError.UNNAMED_LET_BINDING,
      "<container @let-=\"\">"
    );

    makeErrorCase(
      MarkupParseError.UNNAMED_LET_BINDING,
      "<container @let=\"\">"
    );
  }

  @Test
  public void shouldThrowOnBracketedLetBindings() {
    makeErrorCase(
      MarkupParseError.BRACKETED_LET_BINDING,
      "<container @[let-]=\"\">"
    );

    makeErrorCase(
      MarkupParseError.BRACKETED_LET_BINDING,
      "<container @[let-my_var]=\"\">"
    );

    makeErrorCase(
      MarkupParseError.BRACKETED_LET_BINDING,
      "<container @[let]=\"\">"
    );
  }

  @Test
  public void shouldThrowOnUnnamedForLoop() {
    makeErrorCase(
      MarkupParseError.UNNAMED_FOR_LOOP,
      "<container @*for-=\"\">"
    );
  }

  @Test
  public void shouldThrowOnBindingInUse() {
    makeErrorCase(
      MarkupParseError.BINDING_IN_USE,
      "<container let-a=\"one\" @let-a=\"two\">"
    );

    makeErrorCase(
      MarkupParseError.BINDING_IN_USE,
      "<container *for-a=\"one\" @let-a=\"two\">"
    );

    makeErrorCase(
      MarkupParseError.BINDING_IN_USE,
      "<container let-a=\"one\" @*for-a=\"two\">"
    );
  }

  @Test
  public void shouldThrowOnMalformedIdentifier() {
    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container @let-a-b=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container @let-0abc=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container @*for-a-b=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container @*for-0abc=\"one\">"
    );
  }

  @Test
  public void shouldThrowOnUnbalancedAttributeBrackets() {
    makeErrorCase(
      MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container @[hello=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container @hello]=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container @[[hello]=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnMultipleAttributeBrackets() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_BRACKETS,
      "<container @[[hello]]=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnUnbalancedClosingTag() {
    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "<red>hello<green>world</green></red>@</red>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "<red>hello@</blue>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "Hello@</red>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "@</>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "@</*>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "<red>hello</red>@</>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "<red>hello</red>@</*>"
    );
  }

  @Test
  public void shouldThrowOnCloseAfterCloseAll() {
    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "<red><bold>hello, world!</*>@</red>"
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
      MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=5>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=5.5>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=true>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "=false>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + "={}>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
      "<container @*" + attribute + ">"
    );
  }

  @Test
  public void shouldThrowOnNonStringExpressionAttribute() {
    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=5>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=5.5>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=true>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]=false>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container @[attr]={}>"
    );
  }

  @Test
  public void shouldThrowOnExpectedMarkupValue() {
    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<hover-text @value=\"hello, world\">"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<hover-text @value=5>"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<hover-text @value=5.5>"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<hover-text @value=true>"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<hover-text @value=false>"
    );
  }

  @Test
  public void shouldThrowOnExpectedScalarValue() {
    makeErrorCase(
      MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE,
      "<key @key={} />"
    );
  }

  @Test
  public void shouldThrowOnExpectedSelfClosingTag() {
    makeErrorCase(
      MarkupParseError.EXPECTED_SELF_CLOSING_TAG,
      "<key@></key>"
    );
  }

  @Test
  public void shouldThrowOnExpectedOpenCloseTag() {
    makeErrorCase(
      MarkupParseError.EXPECTED_OPEN_CLOSE_TAG,
      "<red /@>"
    );
  }

  @Test
  public void shouldThrowOnExpectedStructuralAttributeFlag() {
    makeErrorCase(
      MarkupParseError.EXPECTED_STRUCTURAL_ATTRIBUTE_FLAG,
      "<red @*else=\"\">"
    );
  }

  @Test
  public void shouldThrowOnNonMarkupForSeparator() {
    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<red *for-member=\"members\" @for-separator=.5>"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<red *for-member=\"members\" @for-separator=5>"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<red *for-member=\"members\" @for-separator=true>"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_MARKUP_ATTRIBUTE_VALUE,
      "<red *for-member=\"members\" @for-separator=\"hello\">"
    );
  }

  @Test
  public void shouldThrowOnNonScalarForReversed() {
    makeErrorCase(
      MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE,
      "<red *for-member=\"members\" @for-reversed={}>"
    );
  }

  @Test
  public void shouldThrowOnMultipleNonMultiAttributes() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-separator={} @for-separator={}>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=true @for-reversed=false>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=.5 @for-reversed=.2>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=5 @for-reversed=2>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" for-reversed=\"hello\" @for-reversed=\"hello\">"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item name={} @name={}>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item name={} @name={}>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item amount=1 @amount=1>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item material=\"first\" @material=\"second\">"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<block-nbt coordinates=\"a\" path=\"b\" interpret=true @interpret=false>"
    );
  }

  @Test
  public void shouldThrowOnMultipleIfElseConditions() {
    String[] conditionals = { "*if=\"true\"", "*else-if=\"true\"", "*else" };

    for (int i = 0; i < conditionals.length; ++i) {
      String firstCondition = conditionals[i];
      for (int j = i + 1; j < conditionals.length; ++j) {
        String secondCondition = conditionals[j];

        makeErrorCase(
          MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS,
          "<red " + firstCondition + " @" + secondCondition + ">"
        );
      }

      makeErrorCase(
        MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS,
        "<red " + firstCondition + " @" + firstCondition + ">"
      );
    }
  }

  @Test
  public void shouldThrowOnMultipleUseConditions() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_USE_CONDITIONS,
      "<red *use=\"a\" @*use=\"b\">"
    );
  }

  @Test
  public void shouldThrowOnMultipleLoops() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_LOOPS,
      "<red *for-member=\"members\" @*for-user=\"users\">"
    );
  }

  @Test
  public void shouldThrowOnMissingPrecedingIfSibling() {
    makeErrorCase(
      MarkupParseError.MISSING_PRECEDING_IF_SIBLING,
      "@<red *else-if=\"true\">"
    );

    makeErrorCase(
      MarkupParseError.MISSING_PRECEDING_IF_SIBLING,
      "@<red *else>"
    );
  }

  @Test
  public void shouldCreateProperErrorScreens() {
    makeErrorScreenCase(
      new TextWithAnchors(
        "<translate",
        "  let-a=\"b\"",
        "  [key]=\"my.expr[222 c.d.e\"",
        "  fallback={",
        "    hello, {user}",
        "  }",
        "/>"
      ),
      // The escaped double-quote adds an extra char to line 3, thus it only looks like
      // the pointer is off by one in code, not when printed later on.
      new TextWithAnchors(
        "1: <translate",
        "2:   let-a=\"b\"",
        "3:   [key]=\"my.expr[222 c.d.e\"",
        "----------------------^",
        "   Error: Expected a closing-bracket after the indexing-invocation: ]",
        "4:   fallback={",
        "5:     hello, {user}",
        "6:   }",
        "7: />"
      )
    );

    makeErrorScreenCase(
      new TextWithAnchors(
        "<red",
        "  let-a=\"b\"",
        ">{ user.'name' }"
      ),
      new TextWithAnchors(
        "1: <red",
        "2:   let-a=\"b\"",
        "3: >{ user.'name' }",
        "-----------^",
        "   Error: The right-hand-side of a member-access operation may only be an identifier: user.<identifier>"
      )
    );

    makeErrorScreenCase(
      new TextWithAnchors(
        "<red",
        "  let-a=\"b\"",
        "/>"
      ),
      new TextWithAnchors(
        "1: <red",
        "2:   let-a=\"b\"",
        "3: />",
        "----^",
        "   Error: This tag requires a separate closing-tag </red>, as it expects content and does not support self-closing <red />"
      )
    );

    makeErrorScreenCase(
      new TextWithAnchors(
        "<red",
        "  my-attr=trruee",
        "/>"
      ),
      new TextWithAnchors(
        "1: <red",
        "2:   my-attr=trruee",
        "-----^",
        "   Error: This true-literal is malformed",
        "3: />"
      )
    );
  }

  @Test
  public void shouldThrowOnMissingMandatoryAttributes() {
    makeErrorCase(
      MarkupParseError.MISSING_MANDATORY_ATTRIBUTE,
      "@<score />"
    );

    makeErrorCase(
      MarkupParseError.MISSING_MANDATORY_ATTRIBUTE,
      "@<score name=\"hello\"/>"
    );
  }

  @Test
  public void shouldThrowOnNonExpressionSpreadOperator() {
    makeErrorCase(
      MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION,
      "<gradient @...color=\"red\">"
    );

    makeErrorCase(
      MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION,
      "<gradient @...color=5.5>"
    );

    makeErrorCase(
      MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION,
      "<gradient @...color=5>"
    );

    makeErrorCase(
      MarkupParseError.SPREAD_DISALLOWED_ON_NON_EXPRESSION,
      "<gradient @...color=true>"
    );
  }

  @Test
  public void shouldThrowOnIsCaseOutsideOfWhenParent() {
    makeErrorCase(
      MarkupParseError.IS_CASE_OUTSIDE_OF_WHEN_PARENT,
      "<red @*is=\"A\">Hello, world!"
    );
  }

  @Test
  public void shouldThrowOnOtherCaseOutsideOfWhenParent() {
    makeErrorCase(
      MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT,
      "<red @*other>Hello, world!"
    );
  }

  @Test
  public void shouldThrowOnNonStringWhenAttributes() {
    String[] values = { "=true", "=false", "=5", "=5.5", "" };

    for (String value : values) {
      makeErrorCase(
        MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
        "<red @*is" + value + ">"
      );

      makeErrorCase(
        MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
        "<red @*when" + value + ">"
      );

      if (!value.isEmpty()) {
        makeErrorCase(
          MarkupParseError.NON_STRING_STRUCTURAL_ATTRIBUTE,
          "<red @*other" + value + ">"
        );
      }
    }
  }

  @Test
  public void shouldThrowOnDuplicateWhenInput() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT,
      "<container *when=\"my.expr\" @*when=\"your.expr\">"
    );
  }

  @Test
  public void shouldThrowOnNoCases() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_NO_CASES,
      "@<container *when=\"my.expr\">"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_NO_CASES,
      "@<container *when=\"my.expr\">",
      "  <red *other>Fallback"
    );
  }

  @Test
  public void shouldThrowOnDuplicateFallback() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DUPLICATE_FALLBACK,
      "<container *when=\"my.expr\">",
      "  <red *other>Fallback 1</>",
      "  @<green *other>Fallback 2</>"
    );
  }

  @Test
  public void shouldThrowOnDuplicateCase() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DUPLICATE_CASE,
      "<container *when=\"my.expr\">",
      "  <red *is=\"ABC\">Case 1</>",
      "  @<green *is=\"abc\">Case 2</>"
    );
  }

  @Test
  public void shouldThrowOnMultipleWhenCaseAttributes() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red *is=\"ABC\" @*is=\"D\">Case"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red *is=\"ABC\" @*other>Case"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red *other @*is=\"ABC\">Case"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red *other @*other>Case"
    );
  }

  @Test
  public void shouldThrowOnDisallowedMatchingMembers() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  @Static content"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  @<red>Static content"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  <red *is=\"A\" @*if=\"b\">Static content"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  <red *is=\"A\" @*else-if=\"b\">Static content"
    );
  }

  private void makeErrorScreenCase(TextWithAnchors input, TextWithAnchors screen) {
    MarkupParseException exception = Assertions.assertThrows(
      MarkupParseException.class,
      () -> MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE)
    );

    List<String> screenLines = exception.makeErrorScreen();
    Assertions.assertEquals(screen.text, String.join("\n", screenLines));
  }

  private void makeErrorCase(MarkupParseError error, String... lines) {
    TextWithAnchors input = new TextWithAnchors(lines);
    Throwable thrownError = null;

    try {
      MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE);
    } catch (Throwable e) {
      thrownError = e;
    }

    Assertions.assertNotNull(thrownError, "Expected an error to be thrown, but got none");

    Throwable finalThrownError = thrownError;

    if (!(thrownError instanceof MarkupParseException))
      throw new AssertionError("Expected an ast parse exception, but got " + finalThrownError.getClass(), finalThrownError);

    CursorPosition position = input.anchor(0);

    Assertions.assertEquals(error, ((MarkupParseException) thrownError).error);
    Assertions.assertEquals(Jsonifier.jsonify(position), Jsonifier.jsonify(((MarkupParseException) thrownError).position));
  }
}
