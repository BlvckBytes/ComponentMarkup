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
            text.subView(0).setLowercase(),
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
        text.subView(0).setLowercase(),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.TAG, text.subView(1).setLowercase(), new StringValue(text.subView(2), "first", false)),
          new ArgumentEntry(ArgumentName.TEAM, text.subView(3).setLowercase(), new StringValue(text.subView(4), "second", false)),
          new ArgumentEntry(ArgumentName.NAME, text.subView(5).setLowercase(), new StringValue(text.subView(6), "third", false))
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
        text.subView(0).setLowercase(),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.TAG, text.subView(1).setLowercase(), new StringValue(text.subView(2), "first", false)),
          new ArgumentEntry(ArgumentName.TEAM, text.subView(3).setLowercase(), new StringValue(text.subView(4), "second", false)),
          new ArgumentEntry(ArgumentName.NAME, text.subView(5).setLowercase(), new StringValue(text.subView(6), "third", false))
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
        text.subView(0).setLowercase(),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.START_X, text.subView(1).setLowercase(), new NumericValue(text.subView(2), 5, false, false, false)),
          new ArgumentEntry(ArgumentName.START_Y, text.subView(3).setLowercase(), new NumericValue(text.subView(4), 12, false, true, false)),
          new ArgumentEntry(ArgumentName.START_Z, text.subView(5).setLowercase(), new NumericValue(text.subView(6), 22, false, false, false))
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
        text.subView(0).setLowercase(),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.START_X, text.subView(1).setLowercase(), new NumericValue(text.subView(2), 3.5, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Y, text.subView(3).setLowercase(), new NumericValue(text.subView(4), 12.312, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Z, text.subView(5).setLowercase(), new NumericValue(text.subView(6), 22.54, true, true, false))
        )
      )
    );

    text = new TextWithSubViews("@`e´[`x´=`.5´,`y´=`.312´,`z´=-`.54´]");

    makeCase(
      text,
      new TargetSelector(
        TargetType.ALL_ENTITIES,
        text.subView(0).setLowercase(),
        Arrays.asList(
          new ArgumentEntry(ArgumentName.START_X, text.subView(1).setLowercase(), new NumericValue(text.subView(2), .5, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Y, text.subView(3).setLowercase(), new NumericValue(text.subView(4), .312, true, false, false)),
          new ArgumentEntry(ArgumentName.START_Z, text.subView(5).setLowercase(), new NumericValue(text.subView(6), .54, true, true, false))
        )
      )
    );
  }

  @Test
  public void shouldParseEmptyStringValues() {
    makeEmptyStringCase(false, false);
    makeEmptyStringCase(false, true);
    makeEmptyStringCase(true, false);
    makeEmptyStringCase(true, true);
  }

  private void makeEmptyStringCase(boolean quoted, boolean negated) {
    TextWithSubViews text = new TextWithSubViews(
      "@`e´[`team´=" + (negated ? "!" : "") + (quoted ? "`\"\"´" : "`´") + "]"
    );

    makeCase(
      text,
      new TargetSelector(
        TargetType.ALL_ENTITIES,
        text.subView(0).setLowercase(),
        Collections.singletonList(
          new ArgumentEntry(ArgumentName.TEAM, text.subView(1).setLowercase(), new StringValue(text.subView(2), "", negated))
        )
      )
    );
  }

  private void makeCase(TextWithSubViews input, TargetSelector expectedSelector) {
    TargetSelector actualSelector = SelectorParser.parse(StringView.of(input.text));
    Assertions.assertEquals(Jsonifier.jsonify(expectedSelector), Jsonifier.jsonify(actualSelector));
  }
}
