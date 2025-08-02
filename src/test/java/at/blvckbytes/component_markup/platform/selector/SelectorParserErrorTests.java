/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.util.StringView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SelectorParserErrorTests {

  @Test
  public void shouldThrowOnMissingAtSymbol() {
    makeErrorCasesAtStart(
      SelectorParseError.MISSING_AT_SYMBOL,
      new TextWithSubViews(""),
      new TextWithSubViews("`!´"),
      new TextWithSubViews("`[´"),
      new TextWithSubViews("`hello, world´")
    );
  }

  @Test
  public void shouldThrowOnUnknownTarget() {
    makeErrorCasesAtStart(
      SelectorParseError.UNKNOWN_TARGET_TYPE,
      new TextWithSubViews("@`z´"),
      new TextWithSubViews("@`example´"),
      new TextWithSubViews("@`random´")
    );
  }

  @Test
  public void shouldThrowOnMissingArgumentsOpeningBracket() {
    makeErrorCasesAtStart(
      SelectorParseError.MISSING_ARGUMENTS_OPENING_BRACKET,
      new TextWithSubViews("@p   `test´"),
      new TextWithSubViews("@p   `\"test\"´"),
      new TextWithSubViews("@p   `.5´"),
      new TextWithSubViews("@p   `23´"),
      new TextWithSubViews("@p   `{´")
    );
  }

  @Test
  public void shouldThrowOnMissingArgumentsClosingBracket() {
    makeErrorCasesAtEnd(
      SelectorParseError.MISSING_ARGUMENTS_CLOSING_BRACKET,
      new TextWithSubViews("`@p[´"),
      new TextWithSubViews("`@p[ ´"),
      new TextWithSubViews("`@p[  ´")
    );
  }

  @Test
  public void shouldThrowOnMissingArgumentName() {
    makeErrorCasesAtStart(
      SelectorParseError.MISSING_ARGUMENT_NAME,
      new TextWithSubViews("@p[`=´]"),
      new TextWithSubViews("@p[limit=1,`=´]")
    );
  }

  @Test
  public void shouldThrowOnDanglingArgumentSeparator() {
    makeErrorCasesAtStart(
      SelectorParseError.DANGLING_ARGUMENT_SEPARATOR,
      new TextWithSubViews("@p[`,´]"),
      new TextWithSubViews("@p[limit=1,`,´]")
    );
  }

  @Test
  public void shouldThrowOnUnknownArgumentNames() {
    makeErrorCasesAtStart(
      SelectorParseError.UNKNOWN_ARGUMENT_NAME,
      new TextWithSubViews("@p[`unknown´=5]"),
      new TextWithSubViews("@p[limit=2,`idk´=hello]")
    );
  }

  @Test
  public void shouldThrowOnMissingEqualsSign() {
    makeErrorCasesAtStart(
      SelectorParseError.MISSING_EQUALS_SIGN,
      new TextWithSubViews("@p[limit` ´2]"),
      new TextWithSubViews("@p[limit=2,x` ´5]"),
      new TextWithSubViews("@p[limit=2,`x´]"),
      new TextWithSubViews("@p[limit=2,`x´")
    );
  }

  @Test
  public void shouldThrowOnMissingArgumentSeparator() {
    makeErrorCasesAtStart(
      SelectorParseError.MISSING_ARGUMENT_SEPARATOR,
      new TextWithSubViews("@p[limit=2` ´x=.45]"),
      new TextWithSubViews("@p[limit=2,x=.45` ´y=23]")
    );
  }

  @Test
  public void shouldThrowOnMissingTargetType() {
    makeErrorCasesAtStart(
      SelectorParseError.MISSING_TARGET_TYPE,
      new TextWithSubViews("`@´"),
      new TextWithSubViews("`@´   ")
    );
  }

  @Test
  public void shouldThrowOnMultiNeverArgument() {
    makeErrorCasesAtStart(
      SelectorParseError.MULTI_NEVER_ARGUMENT,
      new TextWithSubViews("@p[limit=2,`limit´=3]")
    );
  }

  @Test
  public void shouldThrowOnMultiIfNegatedArgument() {
    makeErrorCasesAtStart(
      SelectorParseError.MULTI_IF_NEGATED_ARGUMENT,
      new TextWithSubViews("@p[team=a,`team´=b]")
    );

    Assertions.assertDoesNotThrow(() -> SelectorParser.parse(StringView.of("@p[team=a,team=!b]")));
  }

  @Test
  public void shouldThrowOnStringContainingQuote() {
    makeErrorCasesAtStart(
      SelectorParseError.STRING_CONTAINED_QUOTE,
      new TextWithSubViews("@p[team=hello`\"´world]")
    );

    Assertions.assertDoesNotThrow(() -> SelectorParser.parse(StringView.of("@p[team=hello\\\"world]")));
  }

  @Test
  public void shouldThrowOnUnterminatedString() {
    makeErrorCasesAtStart(
      SelectorParseError.UNTERMINATED_STRING,
      new TextWithSubViews("@p[team=`\"hello world´]"),
      new TextWithSubViews("@p[team=`\"hello world\\\"´]")
    );

    Assertions.assertDoesNotThrow(() -> SelectorParser.parse(StringView.of("@p[team=\"hello\\\"world\"]")));
  }

  private void makeErrorCasesAtStart(SelectorParseError expectedError, TextWithSubViews... inputs) {
    for (TextWithSubViews text : inputs) {
      makeErrorCase(
        text,
        expectedError,
        (text.text.isEmpty())
          ? 0
          : text.subView(0).startInclusive
      );
    }
  }

  private void makeErrorCasesAtEnd(SelectorParseError expectedError, TextWithSubViews... inputs) {
    for (TextWithSubViews text : inputs)
      makeErrorCase(text, expectedError, text.subView(0).endExclusive - 1);
  }

  private void makeErrorCase(TextWithSubViews input, SelectorParseError expectedError, int expectedPosition) {
    SelectorParseException actualException = Assertions.assertThrows(
      SelectorParseException.class,
      () -> SelectorParser.parse(StringView.of(input.text))
    );

    Assertions.assertEquals(expectedError, actualException.error, input.text);
    Assertions.assertEquals(expectedPosition, actualException.position, input.text);
  }

}
