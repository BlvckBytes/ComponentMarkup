/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MarkupParserErrorTests {

  @Test
  public void shouldThrowOnUnknownTag() {
    makeErrorCase(
      MarkupParseError.UNKNOWN_TAG,
      "<`unknown-tag´>"
    );
  }

  @Test
  public void shouldThrowOnUnknownAttribute() {
    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} `unknown´=5>"
    );

    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} `unknown´=5.5>"
    );

    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} `unknown´=\"hello\">"
    );

    makeErrorCase(
      MarkupParseError.UNSUPPORTED_ATTRIBUTE,
      "<hover-text value={} `unknown´={}>"
    );
  }

  @Test
  public void shouldThrowOnUnknownIntrinsicAttribute() {
    makeErrorCase(
      MarkupParseError.UNKNOWN_INTRINSIC_ATTRIBUTE,
      "<container `*unknown´>"
    );
  }

  @Test
  public void shouldThrowOnUnnamedLetBinding() {
    makeErrorCase(
      MarkupParseError.UNNAMED_LET_BINDING,
      "<container `*let-´=\"a\">"
    );

    makeErrorCase(
      MarkupParseError.UNNAMED_LET_BINDING,
      "<container `*let´=\"b\">"
    );
  }

  @Test
  public void shouldThrowOnMultipleIntrinsicMarkers() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_INTRINSIC_MARKERS,
      "<container *`*´if=\"\">"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_INTRINSIC_MARKERS,
      "<container *`+´if=\"\">"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_INTRINSIC_MARKERS,
      "<container +`*´if=\"\">"
    );


    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_INTRINSIC_MARKERS,
      "<container +`+´if=\"\">"
    );
  }

  @Test
  public void shouldThrowOnLateBindingBrackets() {
    makeErrorCase(
      MarkupParseError.LATE_ATTRIBUTE_BRACKETS,
      "<container *`[´if]=\"\">"
    );

    makeErrorCase(
      MarkupParseError.LATE_ATTRIBUTE_BRACKETS,
      "<container +`[´if]=\"\">"
    );
  }

  @Test
  public void shouldThrowOnUnnamedForLoop() {
    makeErrorCase(
      MarkupParseError.UNNAMED_FOR_LOOP,
      "<container `*for-´=\"abc\">"
    );
  }

  @Test
  public void shouldThrowOnEmptyExpressions() {
    makeErrorCase(
      MarkupParseError.EMPTY_EXPRESSION,
      "<container *for-=\"`´\">"
    );
  }

  @Test
  public void shouldThrowOnBindingInUse() {
    makeErrorCase(
      MarkupParseError.BINDING_IN_USE,
      "<container *let-a=\"one\" *let-`a´=\"two\">"
    );

    makeErrorCase(
      MarkupParseError.BINDING_IN_USE,
      "<container *for-a=\"one\" *let-`a´=\"two\">"
    );

    makeErrorCase(
      MarkupParseError.BINDING_IN_USE,
      "<container *let-a=\"one\" *for-`a´=\"two\">"
    );
  }

  @Test
  public void shouldThrowOnMalformedIdentifier() {
    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container *let-`a-b´=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container *let-`_a-b´=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container *let-`0abc´=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container *for-`a-b´=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container *for-`0abc´=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_IDENTIFIER,
      "<container &`hello-world´ />"
    );
  }

  @Test
  public void shouldThrowOnMalformedAttributeName() {
    makeErrorCase(
      MarkupParseError.MALFORMED_ATTRIBUTE_NAME,
      "<container `a_b´=\"one\">"
    );
    makeErrorCase(
      MarkupParseError.MALFORMED_ATTRIBUTE_NAME,
      "<container `_a_b´=\"one\">"
    );

    // This will be caught by the cml-parser first; still - for completeness
    makeErrorCase(
      MarkupParseError.CML_PARSE_ERROR,
      "<container `0abc´=\"one\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_ATTRIBUTE_NAME,
      "<container [`0abc´]=\"one\">"
    );
  }

  @Test
  public void shouldThrowOnUnbalancedAttributeBrackets() {
    makeErrorCase(
      MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container `[´=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container `[´hello=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container `h´ello]=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_ATTRIBUTE_BRACKETS,
      "<container [`[´hello]=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnUnbalancedCaptureParentheses() {
    makeErrorCase(
      MarkupParseError.UNBALANCED_CAPTURE_PARENTHESES,
      "<container *let-`(´my_var={}>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CAPTURE_PARENTHESES,
      "<container *let-my_var`)´={}>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CAPTURE_PARENTHESES,
      "<container *let-(`(´my_var)={}>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CAPTURE_PARENTHESES,
      "<container *let-(my_var`)´)={}>"
    );
  }

  @Test
  public void shouldThrowOnMultipleCaptureParentheses() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_CAPTURE_PARENTHESES,
      "<container *let-(`(´hello))={}>"
    );
  }

  @Test
  public void shouldThrowOnMultipleAttributeBrackets() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_BRACKETS,
      "<container [`[´hello]]=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnEmptyAttributeNames() {
    makeErrorCase(
      MarkupParseError.EMPTY_ATTRIBUTE_NAME,
      "<container `[´]=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.EMPTY_ATTRIBUTE_NAME,
      "<container `[´...]=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.EMPTY_ATTRIBUTE_NAME,
      "<container `&´=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.EMPTY_ATTRIBUTE_NAME,
      "<container `*´=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.EMPTY_ATTRIBUTE_NAME,
      "<container `+´=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.EMPTY_ATTRIBUTE_NAME,
      "<container `[´]=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnMultipleAttributeSpreads() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_SPREADS,
      "<container [...`...´hello]=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnMalformedSpreadOperators() {
    makeErrorCase(
      MarkupParseError.MALFORMED_SPREAD_OPERATOR,
      "<container [`.´hello]=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_SPREAD_OPERATOR,
      "<container [`..´hello]=\"world\">"
    );

    makeErrorCase(
      MarkupParseError.MALFORMED_SPREAD_OPERATOR,
      "<container [...`..´hello]=\"world\">"
    );
  }

  @Test
  public void shouldThrowOnUnbalancedClosingTag() {
    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "<red>hello<green>world</green></red></`red´>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "<red>hello</`blue´>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "Hello</`red´>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "`<´/>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "</`*´>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "<red>hello</red>`<´/>"
    );

    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG_BLANK,
      "<red>hello</red></`*´>"
    );
  }

  @Test
  public void shouldThrowOnCloseAfterCloseAll() {
    makeErrorCase(
      MarkupParseError.UNBALANCED_CLOSING_TAG,
      "<red><bold>hello, world!</*></`red´>"
    );
  }

  @Test
  public void shouldThrowOnSomeIntrinsicMarkupOrFlagAttributeValues() {
    makeMarkupOrFlagAttributeValueCase("if");
    makeMarkupOrFlagAttributeValueCase("else-if");
    makeMarkupOrFlagAttributeValueCase("for-member");
    makeMarkupOrFlagAttributeValueCase("use");
    makeMarkupOrFlagAttributeValueCase("when");
    makeMarkupOrFlagAttributeValueCase("for-test");
  }

  private void makeMarkupOrFlagAttributeValueCase(String attribute) {
    makeErrorCase(
      MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE,
      "<container `*" + attribute + "´={}>"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_NON_FLAG,
      "<container `*" + attribute + "´>"
    );
  }

  @Test
  public void shouldThrowOnNonStringExpressionAttribute() {
    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container [`attr´]=5>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container [`attr´]=5.5>"
    );

    makeErrorCase(
      MarkupParseError.NON_STRING_EXPRESSION_ATTRIBUTE,
      "<container [`attr´]={}>"
    );
  }

  @Test
  public void shouldNotThrowOnNonMarkupValue() {
    makeErrorCase(null, "<hover-text `value´=\"hello, world\">");
    makeErrorCase(null, "<hover-text `value´=5>");
    makeErrorCase(null, "<hover-text `value´=5.5>");
    makeErrorCase(null, "<red *for-member=\"members\" `*for-separator´=.5>");
    makeErrorCase(null, "<red *for-member=\"members\" `*for-separator´=5>");
    makeErrorCase(null, "<red *for-member=\"members\" `*for-separator´=\"hello\">");
  }

  @Test
  public void shouldThrowOnExpectedExpressionValue() {
    makeErrorCase(
      MarkupParseError.EXPECTED_EXPRESSION_ATTRIBUTE_VALUE,
      "<key `key´={} />"
    );
  }

  @Test
  public void shouldThrowOnExpectedSelfClosingTag() {
    makeErrorCase(
      MarkupParseError.EXPECTED_SELF_CLOSING_TAG,
      "<`key´></key>"
    );
  }

  @Test
  public void shouldThrowOnExpectedOpenCloseTag() {
    makeErrorCase(
      MarkupParseError.EXPECTED_OPEN_CLOSE_TAG,
      "<`red´ />"
    );
  }

  @Test
  public void shouldThrowOnExpectedIntrinsicAttributeFlag() {
    makeErrorCase(
      MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG,
      "<red `*else´=\"a\">"
    );

    makeErrorCase(
      MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG,
      "<red `*else´=-2.3>"
    );
  }

  @Test
  public void shouldThrowOnNonExpressionForReversed() {
    makeErrorCase(
      MarkupParseError.NON_EXPRESSION_INTRINSIC_ATTRIBUTE,
      "<red *for-member=\"members\" `*for-reversed´={}>"
    );
  }

  @Test
  public void shouldThrowOnMultipleNonMultiAttributes() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" *for-separator={} `*for-separator´={}>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" *for-reversed `*for-reversed´=false>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" *for-reversed=.5 `*for-reversed´=.2>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" *for-reversed=5 `*for-reversed´=2>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<red *for-member=\"members\" *for-reversed=\"hello\" `*for-reversed´=\"hello\">"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item name={} `name´={}>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item name={} `name´={}>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item amount=1 `amount´=1>"
    );

    makeErrorCase(
      MarkupParseError.MULTIPLE_NON_MULTI_ATTRIBUTE,
      "<hover-item material=\"first\" `material´=\"second\">"
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
          "<red " + firstCondition + " `" + secondCondition + "´>"
        );
      }

      makeErrorCase(
        MarkupParseError.MULTIPLE_IF_ELSE_CONDITIONS,
        "<red " + firstCondition + " `" + firstCondition + "´>"
      );
    }
  }

  @Test
  public void shouldThrowOnMultipleUseConditions() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_USE_CONDITIONS,
      "<red *use=\"a\" `*use´=\"b\">"
    );
  }

  @Test
  public void shouldThrowOnMultipleLoops() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_LOOPS,
      "<red *for-member=\"members\" `*for-user´=\"users\">"
    );
  }

  @Test
  public void shouldThrowOnMissingPrecedingIfSibling() {
    makeErrorCase(
      MarkupParseError.MISSING_PRECEDING_IF_SIBLING,
      "<`red´ *else-if=\"true\">"
    );

    makeErrorCase(
      MarkupParseError.MISSING_PRECEDING_IF_SIBLING,
      "<`red´ *else>"
    );
  }

  @Test
  public void shouldCreateProperErrorScreens() {
    makeErrorScreenCase(
      new TextWithSubViews(
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
      new TextWithSubViews(
        "1: <translate",
        "2:   let-a=\"b\"",
        "3:   [key]=\"my.expr[222 c.d.e\"",
        "----------------------^",
        "Error: Expected a closing-bracket after the indexing-invocation: ]",
        "4:   fallback={",
        "5:     hello, {user}",
        "6:   }",
        "7: />"
      )
    );

    makeErrorScreenCase(
      new TextWithSubViews(
        "<red",
        "  let-a=\"b\"",
        ">{ user.'name' }"
      ),
      new TextWithSubViews(
        "1: <red",
        "2:   let-a=\"b\"",
        "3: >{ user.'name' }",
        "-----------^",
        "Error: The right-hand-side of a member-access operation may only be an identifier: user.<identifier>"
      )
    );

    makeErrorScreenCase(
      new TextWithSubViews(
        "<red",
        "  let-a=\"b\"",
        "/>"
      ),
      new TextWithSubViews(
        "1: <red",
        "----^",
        "Error: This tag requires a separate closing-tag </red>, as it expects content and does not support self-closing <red />",
        "2:   let-a=\"b\"",
        "3: />"
      )
    );

    makeErrorScreenCase(
      new TextWithSubViews(
        "<&7 *if=\"my_value gt 55\">"
      ),
      new TextWithSubViews(
        // We use > instead of gt, so the latter will be interpreted as yet another
        // identifier, which will cause a EXPECTED_EOS to be thrown.
        "1: <&7 *if=\"my_value gt 55\">",
        "---------------------^",
        "Error: Expected the input be over at this point (dangling/trailing tokens?)"
      )
    );
  }

  @Test
  public void shouldThrowOnMissingMandatoryAttributes() {
    makeErrorCase(
      MarkupParseError.MISSING_MANDATORY_ATTRIBUTE,
      "<`key´ />"
    );
  }

  @Test
  public void shouldThrowOnNonExpressionSpreadOperator() {
    makeErrorCase(
      MarkupParseError.SPREAD_DISALLOWED_ON_NON_BINDING,
      "<gradient `...´color=\"red\">"
    );

    makeErrorCase(
      MarkupParseError.SPREAD_DISALLOWED_ON_NON_BINDING,
      "<gradient `...´color=5.5>"
    );

    makeErrorCase(
      MarkupParseError.SPREAD_DISALLOWED_ON_NON_BINDING,
      "<gradient `...´color=5>"
    );
  }

  @Test
  public void shouldThrowOnIsCaseOutsideOfWhenParent() {
    makeErrorCase(
      MarkupParseError.IS_CASE_OUTSIDE_OF_WHEN_PARENT,
      "<red `+is´=\"A\">Hello, world!"
    );
  }

  @Test
  public void shouldThrowOnOtherCaseOutsideOfWhenParent() {
    makeErrorCase(
      MarkupParseError.OTHER_CASE_OUTSIDE_OF_WHEN_PARENT,
      "<red `*other´>Hello, world!"
    );
  }

  @Test
  public void shouldThrowOnWrongValuedWhenAttributes() {
    makeErrorCase(
      MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_NON_FLAG,
      "<red `*when´>"
    );

    String[] values = { "=5", "=false", "=5.5", "" };

    for (String value : values) {
      // is-labels support literals, for convenience
      if (value.isEmpty()) {
        makeErrorCase(
          MarkupParseError.NON_LITERAL_INTRINSIC_ATTRIBUTE,
          "<red `+is´" + value + ">"
        );
      }

      if (!value.isEmpty()) {
        makeErrorCase(
          MarkupParseError.EXPECTED_INTRINSIC_ATTRIBUTE_FLAG,
          "<red `*other´" + value + ">"
        );
      }
    }
  }

  @Test
  public void shouldThrowOnDuplicateWhenInput() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DUPLICATE_INPUT,
      "<container *when=\"my.expr\" `*when´=\"your.expr\">"
    );
  }

  @Test
  public void shouldThrowOnNoCases() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_NO_CASES,
      "<`container´ *when=\"my.expr\">"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_NO_CASES,
      "<`container´ *when=\"my.expr\">",
      "  <red *other>Fallback"
    );
  }

  @Test
  public void shouldThrowOnDuplicateFallback() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DUPLICATE_FALLBACK,
      "<container *when=\"my.expr\">",
      "  <red *other>Fallback 1</>",
      "  <`green´ *other>Fallback 2</>"
    );
  }

  @Test
  public void shouldThrowOnDuplicateCase() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DUPLICATE_CASE,
      "<container *when=\"my.expr\">",
      "  <red +is=\"ABC\">Case 1</>",
      "  <`green´ +is=\"abc\">Case 2</>"
    );
  }

  @Test
  public void shouldThrowOnMultipleWhenCaseAttributes() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red +is=\"ABC\" `+is´=\"D\">Case"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red +is=\"ABC\" `*other´>Case"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red *other `+is´=\"ABC\">Case"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_COLLIDING_CASES,
      "<container *when=\"my.expr\">",
      "  <red *other `*other´>Case"
    );
  }

  @Test
  public void shouldThrowOnDisallowedMatchingMembers() {
    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  `Static content´"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  <`red´>Static content"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  <red +is=\"A\" `*if´=\"b\">Static content"
    );

    makeErrorCase(
      MarkupParseError.WHEN_MATCHING_DISALLOWED_MEMBER,
      "<container *when=\"my.expr\">",
      "  <red +is=\"A\" `*else-if´=\"b\">Static content"
    );
  }

  @Test
  public void shouldThrowOnNonMultiAttributeWithSpreadOperator() {
    makeErrorCase(
      MarkupParseError.SPREAD_ON_NON_MULTI_ATTRIBUTE,
      "<key [...`key´]=\"['first', 'second']\"/>"
    );
  }

  @Test
  public void shouldThrowOnLiteralIntrinsicMarkupAttributes() {
    makeErrorCase(
      MarkupParseError.LITERAL_INTRINSIC_MARKUP_ATTRIBUTE,
      "<red *for=\"1..5\" `+for-separator´={ test }>"
    );
  }

  @Test
  public void shouldThrowOnLiteralIntrinsicTemplateLiteralAttributes() {
    makeErrorCase(
      MarkupParseError.LITERAL_INTRINSIC_TEMPLATE_LITERAL_ATTRIBUTE,
      "<red *for=\"1..5\" `+for-separator´=×`test×`>"
    );
  }

  @Test
  public void shouldThrowOnForAuxiliaryAttributesWithoutPriorLoop() {
    makeErrorCase(
      MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE,
      "<red `*for-separator´={ test }>"
    );

    makeErrorCase(
      MarkupParseError.AUXILIARY_FOR_INTRINSIC_ATTRIBUTE,
      "<red `*for-reversed´>"
    );
  }

  @Test
  public void shouldThrowOnBindingNamesWhichAreReservedForOperators() {
    makeErrorCase(
      MarkupParseError.RESERVED_IDENTIFIER,
      "<red *for-`split´=\"a\">"
    );

    makeErrorCase(
      MarkupParseError.RESERVED_IDENTIFIER,
      "<red *let-`split´=\"a\">"
    );

    makeErrorCase(
      MarkupParseError.RESERVED_IDENTIFIER,
      "<red *let-`true´=\"a\">"
    );

    makeErrorCase(
      MarkupParseError.RESERVED_IDENTIFIER,
      "<red *let-`false´=\"a\">"
    );

    makeErrorCase(
      MarkupParseError.RESERVED_IDENTIFIER,
      "<red *let-`null´=\"a\">"
    );
  }

  @Test
  public void shouldThrowOnAttributeUsedWithNonSelfClosingContainer() {
    makeErrorCase(
      MarkupParseError.CONTAINER_ATTRIBUTES_ON_SELF_CLOSING,
      "<`container´ asd={<red>Hello, world!}>"
    );

    makeErrorCase(
      MarkupParseError.CONTAINER_ATTRIBUTES_ON_SELF_CLOSING,
      "<`container´ value={<red>Hello, world!}>"
    );

    makeErrorCase(
      MarkupParseError.CONTAINER_ATTRIBUTES_ON_SELF_CLOSING,
      "<`container´ value={<red>Hello, world!}><green>Actual content!"
    );
  }

  @Test
  public void shouldThrowOnMultipleBindByNameOperators() {
    makeErrorCase(
      MarkupParseError.MULTIPLE_ATTRIBUTE_BIND_BY_NAME_OPERATORS,
      "<container &`&´test />"
    );
  }

  @Test
  public void shouldThrowOnCombinedBindByNameOperator() {
    makeErrorCase(
      MarkupParseError.COMBINED_ATTRIBUTE_BIND_BY_NAME_OPERATOR,
      "<container [`&´test] />"
    );
  }

  @Test
  public void shouldThrowOnCombinedIntrinsicOperator() {
    makeErrorCase(
      MarkupParseError.COMBINED_INTRINSIC_MARKER_OPERATOR,
      "<container [`*´test] />"
    );
  }

  private void makeErrorScreenCase(TextWithSubViews input, TextWithSubViews screen) {
    MarkupParseException exception = Assertions.assertThrows(
      MarkupParseException.class,
      () -> MarkupParser.parse(InputView.of(input.text), BuiltInTagRegistry.INSTANCE)
    );

    List<String> screenLines = exception.makeErrorScreen();
    Assertions.assertEquals(screen.text, String.join("\n", screenLines));
  }

  private void makeErrorCase(@Nullable MarkupParseError error, String... lines) {
    TextWithSubViews input = new TextWithSubViews(lines);
    Throwable thrownError = null;

    try {
      MarkupParser.parse(InputView.of(input.text), BuiltInTagRegistry.INSTANCE);
    } catch (Throwable e) {
      thrownError = e;
    }

    if (error == null) {
      Assertions.assertNull(thrownError, "Expected no error to be thrown");
      return;
    }

    Assertions.assertNotNull(thrownError, "Expected an error to be thrown, but got none");

    Throwable finalThrownError = thrownError;

    if (!(thrownError instanceof MarkupParseException))
      throw new AssertionError("Expected an ast parse exception, but got " + finalThrownError.getClass(), finalThrownError);

    int position = input.subView(0).startInclusive;

    Assertions.assertEquals(error, ((MarkupParseException) thrownError).error);
    Assertions.assertEquals(position, ((MarkupParseException) thrownError).position);
  }
}
