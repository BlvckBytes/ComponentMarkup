/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.platform.selector.argument.*;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.StringView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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

  // TODO: Add case which checks escaped and unescaped quotes within unquoted strings

  @Test
  public void shouldParseUnquotedStringArguments() {
    TextWithSubViews text = new TextWithSubViews("@`e´[`tag´=`first´,`team´=`second´,`name´=`third´]");

    makeCase(
      text,
      new TargetSelector(
        TargetType.ALL_ENTITIES,
        text.subView(0),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.TAG, text.subView(1), new StringValue(text.subView(2), false)),
          new ArgumentEntry(ArgumentName.TEAM, text.subView(3), new StringValue(text.subView(4), false)),
          new ArgumentEntry(ArgumentName.NAME, text.subView(5), new StringValue(text.subView(6), false))
        )
      )
    );
  }

  // TODO: Add case which checks escaped and unescaped quotes within quoted strings

  @Test
  public void shouldParseQuotedStringArguments() {
    TextWithSubViews text = new TextWithSubViews("@`e´[`tag´=`\"first\"´,`team´=`\"second\"´,`name´=`\"third\"´]");

    makeCase(
      text,
      new TargetSelector(
        TargetType.ALL_ENTITIES,
        text.subView(0),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.TAG, text.subView(1), new StringValue(text.subView(2), false)),
          new ArgumentEntry(ArgumentName.TEAM, text.subView(3), new StringValue(text.subView(4), false)),
          new ArgumentEntry(ArgumentName.NAME, text.subView(5), new StringValue(text.subView(6), false))
        )
      )
    );
  }

  @Test
  public void shouldParseIntegerRange() {
  }

  @Test
  public void shouldParseIntegerArguments() {
    TextWithSubViews text = new TextWithSubViews("@`e´[`x´=`5´,`y´=-`12´,`z´=`22´]");

    makeCase(
      text,
      new TargetSelector(
        TargetType.ALL_ENTITIES,
        text.subView(0),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.START_X, text.subView(1), new NumericValue(text.subView(2), 5, false, false, false)),
          new ArgumentEntry(ArgumentName.START_Y, text.subView(3), new NumericValue(text.subView(4), 12, false, true, false)),
          new ArgumentEntry(ArgumentName.START_Z, text.subView(5), new NumericValue(text.subView(6), 22, false, false, false))
        )
      )
    );
  }

  @Test
  public void shouldParseDoubleArguments() {
    TextWithSubViews text = new TextWithSubViews("@`e´[`x´=`3.5´,`y´=`12.312´,`z´=-`22.54´]");

    makeCase(
      text,
      new TargetSelector(
        TargetType.ALL_ENTITIES,
        text.subView(0),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.START_X, text.subView(1), new NumericValue(text.subView(2), 3.5, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Y, text.subView(3), new NumericValue(text.subView(4), 12.312, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Z, text.subView(5), new NumericValue(text.subView(6), 22.54, true, true, false))
        )
      )
    );

    text = new TextWithSubViews("@`e´[`x´=`.5´,`y´=`.312´,`z´=-`.54´]");

    makeCase(
      text,
      new TargetSelector(
        TargetType.ALL_ENTITIES,
        text.subView(0),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.START_X, text.subView(1), new NumericValue(text.subView(2), .5, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Y, text.subView(3), new NumericValue(text.subView(4), .312, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Z, text.subView(5), new NumericValue(text.subView(6), .54, true, true, false))
        )
      )
    );
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
