/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.StringView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class SelectorParserTests {

  /*
    TODO: Create cases for the following yet-missing types
      - MISSING_ARGUMENT_NAME
      - DANGLING_ARGUMENT_SEPARATOR
      - UNKNOWN_ARGUMENT_NAME
      - MISSING_EQUALS_SIGN
      - MISSING_ARGUMENT_SEPARATOR
   */

  @Test
  public void shouldParseAllSelectorTypes() {
    String[][] spacingCases = {
      { "", "" },
      { "  ", "" },
      { "", "  " },
      { "  ", "  " },
    };

    for (String[] spacingCase : spacingCases) {
      for (TargetType type : TargetType.VALUES) {
        TextWithSubViews text = new TextWithSubViews(spacingCase[0] + "@`" + type.character + "´" + spacingCase[1]);

        makeCase(
          text,
          new TargetSelector(
            type,
            text.subView(0),
            Collections.emptyList()
          )
        );
      }
    }
  }

  @Test
  public void shouldThrowOnMissingAtSymbol() {
    makeErrorCase(new TextWithSubViews(""), SelectorParseError.MISSING_AT_SYMBOL, 0);
    makeErrorCase(new TextWithSubViews("!"), SelectorParseError.MISSING_AT_SYMBOL, 0);
    makeErrorCase(new TextWithSubViews("["), SelectorParseError.MISSING_AT_SYMBOL, 0);
    makeErrorCase(new TextWithSubViews("hello, world"), SelectorParseError.MISSING_AT_SYMBOL, 0);
  }

  @Test
  public void shouldThrowOnUnknownTarget() {
    String[] unknownTargets = { "z", "example", "random" };

    for (String unknownTarget : unknownTargets)
      makeErrorCase(new TextWithSubViews("@" + unknownTarget), SelectorParseError.UNKNOWN_TARGET_TYPE, 1);
  }

  @Test
  public void shouldThrowOnMissingArgumentsOpeningBracket() {
    String[] trailingCharCases = {
      "   `test´",
      "   `\"test\"´",
      "   `.5´",
      "   `23´",
      "   `{´",
    };

    for (String trailingCharCase : trailingCharCases) {
      TextWithSubViews text = new TextWithSubViews("@p" + trailingCharCase);

      makeErrorCase(
        text,
        SelectorParseError.MISSING_ARGUMENTS_OPENING_BRACKET,
        text.subView(0).startInclusive
      );
    }
  }

  @Test
  public void shouldThrowOnMissingArgumentsClosingBracket() {
    String[] spacingCases = { "", " ", "  " };

    for (String spacingCase : spacingCases) {
      TextWithSubViews text = new TextWithSubViews("`@p[" + spacingCase + "´");

      makeErrorCase(
        text,
        SelectorParseError.MISSING_ARGUMENTS_CLOSING_BRACKET,
        text.subView(0).endExclusive - 1
      );
    }
  }

  private void makeErrorCase(TextWithSubViews input, SelectorParseError expectedError, int expectedPosition) {
    SelectorParseException actualException = Assertions.assertThrows(
      SelectorParseException.class,
      () -> SelectorParser.parse(StringView.of(input.text))
    );

    Assertions.assertEquals(expectedError, actualException.error);
    Assertions.assertEquals(expectedPosition, actualException.position);
  }

  private void makeCase(TextWithSubViews input, TargetSelector expectedSelector) {
    TargetSelector actualSelector = SelectorParser.parse(StringView.of(input.text));
    Assertions.assertEquals(Jsonifier.jsonify(expectedSelector), Jsonifier.jsonify(actualSelector));
  }
}
