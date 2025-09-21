/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml;

import at.blvckbytes.component_markup.expression.parser.ExpressionParserTests;
import at.blvckbytes.component_markup.markup.cml.event.*;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CmlEventParserTests {

  // ================================================================================
  // Event-sequence tests
  // ================================================================================

  @Test
  public void shouldParseNoAttributesOpeningWithContent() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`Hello, world! :)´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.LAST_TEXT), "Hello, world! :)"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseAttributesOpeningWithContent() {
    TextWithSubViews text = new TextWithSubViews(
      "  <`red´ `attr-1´=\"` string´\" `attr-2´ `!attr-3´ `attr-4´=`.3´ `attr-5´=`-3´>` my content´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(text.subView(1), text.subView(2), "attr-1", " string"),
      new FlagAttributeEvent(text.subView(3), "attr-2"),
      new FlagAttributeEvent(text.subView(4), "!attr-3"),
      new DoubleAttributeEvent(text.subView(5), text.subView(6), .3, "attr-4", ".3"),
      new LongAttributeEvent(text.subView(7), text.subView(8), -3, "attr-5", "-3"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(9).setBuildFlags(SubstringFlag.LAST_TEXT), " my content"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseMultilineAttributesOpeningWithContent() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´",
      "  `attr-1´=\"`value 1´\"",
      "  `attr-2´=\"`value 2´\"",
      "  `attr-3´=\"`value 3´\"",
      ">"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(text.subView(1), text.subView(2), "attr-1", "value 1"),
      new StringAttributeEvent(text.subView(3), text.subView(4), "attr-2", "value 2"),
      new StringAttributeEvent(text.subView(5), text.subView(6), "attr-3", "value 3"),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseSelfClosingTag() {
    TextWithSubViews text = new TextWithSubViews(
      "<  `br´ />"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "br"),
      new TagOpenEndEvent(text.subView(0), true),
      new InputEndEvent()
    );

    text = new TextWithSubViews(
      "<`br´/>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "br"),
      new TagOpenEndEvent(text.subView(0), true),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseOpeningAndClosingTagWithText() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`Hello´`<´/`red´>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello"),
      new TagCloseEvent(text.subView(3), text.subView(2).startInclusive, "red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseIndentedTags() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>",
      "  <`bold´>`hi´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TagOpenBeginEvent(text.subView(1), "bold"),
      new TagOpenEndEvent(text.subView(1), false),
      new TextEvent(text.subView(2).setBuildFlags(SubstringFlag.LAST_TEXT), "hi"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldRemoveTrailingWhitespace() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`Hello ",
      "world ",
      "test´<`bold´>",
      "`test2´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Helloworldtest"),
      new TagOpenBeginEvent(text.subView(2), "bold"),
      new TagOpenEndEvent(text.subView(2), false),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), "test2"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseMarkupAttribute() {
    TextWithSubViews text = new TextWithSubViews(
      "<`tag-outer´",
      "  `attr-1´=`{´",
      "    <`red´>`Hello curly `\\´} bracket´`<´/`red´>",
      "  }",
      ">"
    );

    text.addViewIndexToBeRemoved(text.subView(5).startInclusive);

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "tag-outer"),
      new TagAttributeBeginEvent(text.subView(1), text.subView(2).startInclusive, "attr-1"),
      new TagOpenBeginEvent(text.subView(3), "red"),
      new TagOpenEndEvent(text.subView(3), false),
      new TextEvent(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello curly } bracket"),
      new TagCloseEvent(text.subView(7), text.subView(6).startInclusive, "red"),
      new TagAttributeEndEvent(text.subView(1)),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseInterpolationExpressions() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`Hello, ´`{user.name}´`!´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello, "),
      new InterpolationEvent(text.subView(2), "{user.name}"),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), "!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseFairlyComplexExpression() {
    TextWithSubViews text = new TextWithSubViews(
      "<`show-item´",
      "  `name´=`{´<`red´>`My item!´`<´/`red´>}",
      "  `lore´=`{´<`blue´>`First line´<`br´/>",
      "       <`green´>`Second line´<`br´/>",
      "       <`gray´",
      "         `*for-member´=\"`members´\"",
      "         `limit´=`5´",
      "         `separator´=`{´<`br´/>}",
      "         `empty´=`{´<`red´>`No items found!´}",
      "       >`- ´<`yellow´>`{ member.item }´`<´/`gray´><`br´/>",
      "       <`gray´>`Last line! :)",
      "  ´}",
      ">`hover over ´`{\"me\"}´`! ´<`red´ `my_flag´>`:)´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "show-item"),
      new TagAttributeBeginEvent(text.subView(1), text.subView(2).startInclusive, "name"),
      new TagOpenBeginEvent(text.subView(3), "red"),
      new TagOpenEndEvent(text.subView(3), false),
      new TextEvent(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT), "My item!"),
      new TagCloseEvent(text.subView(6), text.subView(5).startInclusive, "red"),
      new TagAttributeEndEvent(text.subView(1)),
      new TagAttributeBeginEvent(text.subView(7), text.subView(8).startInclusive, "lore"),
      new TagOpenBeginEvent(text.subView(9), "blue"),
      new TagOpenEndEvent(text.subView(9), false),
      new TextEvent(text.subView(10).setBuildFlags(SubstringFlag.INNER_TEXT), "First line"),
      new TagOpenBeginEvent(text.subView(11), "br"),
      new TagOpenEndEvent(text.subView(11), true),
      new TagOpenBeginEvent(text.subView(12), "green"),
      new TagOpenEndEvent(text.subView(12), false),
      new TextEvent(text.subView(13).setBuildFlags(SubstringFlag.INNER_TEXT), "Second line"),
      new TagOpenBeginEvent(text.subView(14), "br"),
      new TagOpenEndEvent(text.subView(14), true),
      new TagOpenBeginEvent(text.subView(15), "gray"),
      new StringAttributeEvent(text.subView(16), text.subView(17), "*for-member", "members"),
      new LongAttributeEvent(text.subView(18), text.subView(19), 5, "limit", "5"),
      new TagAttributeBeginEvent(text.subView(20), text.subView(21).startInclusive, "separator"),
      new TagOpenBeginEvent(text.subView(22), "br"),
      new TagOpenEndEvent(text.subView(22), true),
      new TagAttributeEndEvent(text.subView(20)),
      new TagAttributeBeginEvent(text.subView(23), text.subView(24).startInclusive, "empty"),
      new TagOpenBeginEvent(text.subView(25), "red"),
      new TagOpenEndEvent(text.subView(25), false),
      new TextEvent(text.subView(26).setBuildFlags(SubstringFlag.LAST_TEXT), "No items found!"),
      new TagAttributeEndEvent(text.subView(23)),
      new TagOpenEndEvent(text.subView(15), false),
      new TextEvent(text.subView(27).setBuildFlags(SubstringFlag.INNER_TEXT), "- "),
      new TagOpenBeginEvent(text.subView(28), "yellow"),
      new TagOpenEndEvent(text.subView(28), false),
      new InterpolationEvent(text.subView(29), "{ member.item }"),
      new TagCloseEvent(text.subView(31), text.subView(30).startInclusive, "gray"),
      new TagOpenBeginEvent(text.subView(32), "br"),
      new TagOpenEndEvent(text.subView(32), true),
      new TagOpenBeginEvent(text.subView(33), "gray"),
      new TagOpenEndEvent(text.subView(33), false),
      new TextEvent(text.subView(34).setBuildFlags(SubstringFlag.LAST_TEXT), "Last line! :)"),
      new TagAttributeEndEvent(text.subView(7)),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(35).setBuildFlags(SubstringFlag.INNER_TEXT), "hover over "),
      new InterpolationEvent(text.subView(36), "{\"me\"}"),
      new TextEvent(text.subView(37).setBuildFlags(SubstringFlag.INNER_TEXT), "! "),
      new TagOpenBeginEvent(text.subView(38), "red"),
      new FlagAttributeEvent(text.subView(39), "my_flag"),
      new TagOpenEndEvent(text.subView(38), false),
      new TextEvent(text.subView(40).setBuildFlags(SubstringFlag.LAST_TEXT), ":)"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseInterpolationWithCurlyBracketsInStrings() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`Hello, ´`{user.name + \"}\" + '}'}´`!´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello, "),
      new InterpolationEvent(text.subView(2), "{user.name + \"}\" + '}'}"),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), "!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldHandleAllTypesOfTextLocations() {
    TextWithSubViews text = new TextWithSubViews(
      "`  abcde ´<`red´>` hello ´<`blue´>` world!  ´"
    );

    makeCase(
      text,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT), "abcde "),
      new TagOpenBeginEvent(text.subView(1), "red"),
      new TagOpenEndEvent(text.subView(1), false),
      new TextEvent(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT), " hello "),
      new TagOpenBeginEvent(text.subView(3), "blue"),
      new TagOpenEndEvent(text.subView(3), false),
      new TextEvent(text.subView(4).setBuildFlags(SubstringFlag.LAST_TEXT), " world!"),
      new InputEndEvent()
    );

    text = new TextWithSubViews(
      "`  abcde ´"
    );

    makeCase(
      text,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.ONLY_TEXT), "abcde"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldStripTrailingSpaces() {
    TextWithSubViews text = new TextWithSubViews(
      "`Online players:   ",
      "´<`red´>`test´"
    );

    makeCase(
      text,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT), "Online players:"),
      new TagOpenBeginEvent(text.subView(1), "red"),
      new TagOpenEndEvent(text.subView(1), false),
      new TextEvent(text.subView(2).setBuildFlags(SubstringFlag.LAST_TEXT), "test"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldRemoveNewlineTrailingSpaces() {
    TextWithSubViews text = new TextWithSubViews(
      "`  hello",
      "    world",
      "   test´"
    );

    makeCase(
      text,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.ONLY_TEXT), "helloworldtest"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveSurroundingContentSpaces() {
    TextWithSubViews text = new TextWithSubViews(
      "<`bold´>",
      "  <`red´>`  surrounding spaces  ´`<´/`red´>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "bold"),
      new TagOpenEndEvent(text.subView(0), false),
      new TagOpenBeginEvent(text.subView(1), "red"),
      new TagOpenEndEvent(text.subView(1), false),
      new TextEvent(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT), "  surrounding spaces  "),
      new TagCloseEvent(text.subView(4), text.subView(3).startInclusive, "red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveSurroundingInterpolationSpaces() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`Hello ´`{user.name}´` world!´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello "),
      new InterpolationEvent(text.subView(2), "{user.name}"),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), " world!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeCharactersInAttributeValues() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´ `a´=\"`hello `\\´\" quote´\">"
    );

    text.addViewIndexToBeRemoved(text.subView(3).startInclusive);

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(text.subView(1), text.subView(2), "a", "hello \" quote"),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );

    text = new TextWithSubViews(
      "<`red´ `a´=\"`these > should < not require escaping´\">"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(text.subView(1), text.subView(2), "a", "these > should < not require escaping"),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );

    text = new TextWithSubViews(
      "<`red´ `a´=`{´",
      "  <`green´ `b´=\"`neither } should { these´\">",
      "}>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagAttributeBeginEvent(text.subView(1), text.subView(2).startInclusive, "a"),
      new TagOpenBeginEvent(text.subView(3), "green"),
      new StringAttributeEvent(text.subView(4), text.subView(5), "b", "neither } should { these"),
      new TagOpenEndEvent(text.subView(3), false),
      new TagAttributeEndEvent(text.subView(1)),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeLeadingCharacterInText() {
    TextWithSubViews text = new TextWithSubViews(
      "``\\´<hello, world!´"
    );

    text.addViewIndexToBeRemoved(text.subView(1).startInclusive);

    makeCase(
      text,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.ONLY_TEXT), "<hello, world!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeCharactersInText() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`' escaping \" closing \\> opening `\\´<; closing `\\´} opening `\\´{ \" and '´`<´/`red´>"
    );

    text.addViewIndexToBeRemoved(text.subView(2).startInclusive);
    text.addViewIndexToBeRemoved(text.subView(3).startInclusive);
    text.addViewIndexToBeRemoved(text.subView(4).startInclusive);

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(
        text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT),
        // Within text-content, there's no need to escape quotes, as strings only occur
        // at values of attributes; also, there's no need to escape closing pointy-brackets,
        // as the predecessor tag (if any) is already closed.
        // I am >not< looking for general "consistency" here, but rather want to keep it as
        // syntactically terse as possible, while maintaining readability - thus, only escape
        // what's absolutely required as to avoid ambiguity while parsing.
        "' escaping \" closing \\> opening <; closing } opening { \" and '"
      ),
      new TagCloseEvent(text.subView(6), text.subView(5).startInclusive, "red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveNonEscapingBackslashes() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´ `a´=\"`a \\ backslash´\">`another \\ backslash´"
    );

    // Because why not? :) There's absolutely no reason to treat backslashes which do not
    // actually escape critical characters any differently. I do not care about being "compatible"
    // with some general syntax out there, because I'm building a DSL!

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(text.subView(1), text.subView(2), "a", "a \\ backslash"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), "another \\ backslash"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseFlagAttributes() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´ `a´ `b´>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new FlagAttributeEvent(text.subView(1), "a"),
      new FlagAttributeEvent(text.subView(2), "b"),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldConsumeComments() {
    TextWithSubViews text = new TextWithSubViews(
      "<!-- My comment! :) -->"
    );

    makeCase(
      text,
      new InputEndEvent()
    );

    text = new TextWithSubViews(
      "<!-- A shiny new container! -->",
      "<`container´>",
      "  <!-- An indented comment, :) -->",
      "  <`red´>`Hello, world! ´<!-- Trailing comment -->",
      "<!-- Trailing comment -->` more text´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "container"),
      new TagOpenEndEvent(text.subView(0), false),
      new TagOpenBeginEvent(text.subView(1), "red"),
      new TagOpenEndEvent(text.subView(1), false),
      new TextEvent(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello, world! "),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), " more text"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldNotEmitEmptyStrings() {
    TextWithSubViews text = new TextWithSubViews(
      "<`container´ `*let-test´=`{´ `{a}´` and ´`{b}´ }>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "container"),
      new TagAttributeBeginEvent(text.subView(1), text.subView(2).startInclusive, "*let-test"),
      new InterpolationEvent(text.subView(3), "{a}"),
      new TextEvent(text.subView(4).setBuildFlags(SubstringFlag.INNER_TEXT), " and "),
      new InterpolationEvent(text.subView(5), "{b}"),
      new TagAttributeEndEvent(text.subView(1)),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  // ================================================================================
  // Exception tests
  // ================================================================================

  @Test
  public void shouldThrowOnUnterminatedOpeningTag() {
    TextWithSubViews text = new TextWithSubViews(
      "`<´`red´"
    );

    makeCase(
      text,
      CmlParseError.UNTERMINATED_TAG,
      text.subView(0).startInclusive,
      new TagOpenBeginEvent(text.subView(1), "red")
    );
  }

  @Test
  public void shouldThrowOnUnterminatedClosingTag() {
    TextWithSubViews text = new TextWithSubViews(
      "`<´/red"
    );

    makeCase(
      text,
      CmlParseError.UNTERMINATED_TAG,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnUnterminatedMarkupValue() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´",
      "  `my-attr´=`{´",
      "    <`green´>`Hello, `\\´} world!",
      ">´"
    );

    text.addViewIndexToBeRemoved(text.subView(5).startInclusive);

    makeCase(
      text,
      CmlParseError.UNTERMINATED_MARKUP_VALUE,
      text.subView(2).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagAttributeBeginEvent(text.subView(1), text.subView(2).startInclusive, "my-attr"),
      new TagOpenBeginEvent(text.subView(3), "green"),
      new TagOpenEndEvent(text.subView(3), false),
      new TextEvent(text.subView(4).setBuildFlags(SubstringFlag.LAST_TEXT), "Hello, } world!>")
    );
  }

  @Test
  public void shouldParseAllAttributeTypesWedgedWithTheTagEnd() {
    TextWithSubViews text;

    for (String tagEnd : new String[]{ "/>", ">" }) {
      text = new TextWithSubViews("<`my-tag´ `my-attr´" + tagEnd);

      makeCase(
        text,
        new TagOpenBeginEvent(text.subView(0), "my-tag"),
        new FlagAttributeEvent(text.subView(1), "my-attr"),
        new TagOpenEndEvent(text.subView(0), tagEnd.charAt(0) == '/'),
        new InputEndEvent()
      );

      text = new TextWithSubViews("<`my-tag´ `my-attr´=\"`my-string´\"" + tagEnd);

      makeCase(
        text,
        new TagOpenBeginEvent(text.subView(0), "my-tag"),
        new StringAttributeEvent(text.subView(1), text.subView(2), "my-attr", "my-string"),
        new TagOpenEndEvent(text.subView(0), tagEnd.charAt(0) == '/'),
        new InputEndEvent()
      );

      text = new TextWithSubViews("<`my-tag´ `my-attr´=`512´" + tagEnd);

      makeCase(
        text,
        new TagOpenBeginEvent(text.subView(0), "my-tag"),
        new LongAttributeEvent(text.subView(1), text.subView(2), 512, "my-attr", "512"),
        new TagOpenEndEvent(text.subView(0), tagEnd.charAt(0) == '/'),
        new InputEndEvent()
      );

      text = new TextWithSubViews("<`my-tag´ `my-attr´=`.512´" + tagEnd);

      makeCase(
        text,
        new TagOpenBeginEvent(text.subView(0), "my-tag"),
        new DoubleAttributeEvent(text.subView(1), text.subView(2), 0.512, "my-attr", ".512"),
        new TagOpenEndEvent(text.subView(0), tagEnd.charAt(0) == '/'),
        new InputEndEvent()
      );

      text = new TextWithSubViews("<`my-tag´ `my-attr´=`{´<`red´>`Hello´}" + tagEnd);

      makeCase(
        text,
        new TagOpenBeginEvent(text.subView(0), "my-tag"),
        new TagAttributeBeginEvent(text.subView(1), text.subView(2).startInclusive, "my-attr"),
        new TagOpenBeginEvent(text.subView(3), "red"),
        new TagOpenEndEvent(text.subView(3), false),
        new TextEvent(text.subView(4).setBuildFlags(SubstringFlag.LAST_TEXT), "Hello"),
        new TagAttributeEndEvent(text.subView(1)),
        new TagOpenEndEvent(text.subView(0), tagEnd.charAt(0) == '/'),
        new InputEndEvent()
      );
    }
  }

  @Test
  public void shouldThrowOnUnterminatedInterpolation() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´>`{´user.name + \"}\" + '}'"
    );

    makeCase(
      text,
      CmlParseError.UNTERMINATED_INTERPOLATION,
      text.subView(1).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false)
    );

    text = new TextWithSubViews(
      "<`red´>`{´user.name{"
    );

    makeCase(
      text,
      CmlParseError.UNTERMINATED_INTERPOLATION,
      text.subView(1).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false)
    );

    text = new TextWithSubViews(
      "<`red´>`{´a{"
    );

    makeCase(
      text,
      CmlParseError.UNTERMINATED_INTERPOLATION,
      text.subView(1).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false)
    );
  }

  @Test
  public void shouldThrowOnMalformedNumbers() {
    makeMalformedAttributeValueCase(CmlParseError.MALFORMED_NUMBER, ".");
    makeMalformedAttributeValueCase(CmlParseError.MALFORMED_NUMBER, "-");
    makeMalformedAttributeValueCase(CmlParseError.MALFORMED_NUMBER, "--");
    makeMalformedAttributeValueCase(CmlParseError.MALFORMED_NUMBER, ".5.5");
    makeMalformedAttributeValueCase(CmlParseError.MALFORMED_NUMBER, "- 5.5");
    makeMalformedAttributeValueCase(CmlParseError.MALFORMED_NUMBER, "5AB");
  }

  @Test
  public void shouldThrowOnUnsupportedAttributeValues() {
    makeMalformedAttributeValueCase(CmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "abc");
    makeMalformedAttributeValueCase(CmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "tru");
    makeMalformedAttributeValueCase(CmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "truea");
    makeMalformedAttributeValueCase(CmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "fals");
    makeMalformedAttributeValueCase(CmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "falsea");
    makeMalformedAttributeValueCase(CmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "nul");
    makeMalformedAttributeValueCase(CmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "nulla");
  }

  @Test
  public void shouldThrowOnMissingTagName() {
    TextWithSubViews text = new TextWithSubViews("`<´>");
    makeCase(text, CmlParseError.MISSING_TAG_NAME, text.subView(0).startInclusive);
  }

  @Test
  public void shouldAllowNullNamedClosingTag() {
    TextWithSubViews text = new TextWithSubViews("`<´/>");

    makeCase(
      text,
      new TagCloseEvent(null, text.subView(0).startInclusive, null),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldThrowOnMalformedAttributeKeys() {
    TextWithSubViews text = new TextWithSubViews("<`red´ `my-attr´ `5var´>");

    makeCase(
      text,
      CmlParseError.EXPECTED_ATTRIBUTE_KEY,
      text.subView(2).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new FlagAttributeEvent(text.subView(1), "my-attr")
    );

    text = new TextWithSubViews("<`red´ `my-attr´ `\"my-string\"´>");

    makeCase(
      text,
      CmlParseError.EXPECTED_ATTRIBUTE_KEY,
      text.subView(2).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new FlagAttributeEvent(text.subView(1), "my-attr")
    );
  }

  @Test
  public void shouldThrowOnUnescapedCurlyBrackets() {
    TextWithSubViews text = new TextWithSubViews("hello `}´ world");

    makeCase(
      text,
      CmlParseError.UNESCAPED_CURLY,
      text.subView(0).startInclusive
    );
  }

  @Test
  public void shouldThrowOnMalformedComments() {
    TextWithSubViews text = new TextWithSubViews("`<!-- Hello, world´");
    makeCase(text, CmlParseError.MALFORMED_COMMENT, text.subView(0).startInclusive);

    text = new TextWithSubViews("`<!-- Hello, world -´");
    makeCase(text, CmlParseError.MALFORMED_COMMENT, text.subView(0).startInclusive);

    text = new TextWithSubViews("`<!-- Hello, world ->´");
    makeCase(text, CmlParseError.MALFORMED_COMMENT, text.subView(0).startInclusive);

    text = new TextWithSubViews("`<!-- Hello, world --´");
    makeCase(text, CmlParseError.MALFORMED_COMMENT, text.subView(0).startInclusive);
  }

  @Test
  public void shouldThrowOnEmptyInterpolation() {
    TextWithSubViews text = new TextWithSubViews("`hello ´`{}´ world");

    makeCase(
      text,
      CmlParseError.EMPTY_INTERPOLATION,
      text.subView(1).startInclusive,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT), "hello ")
    );

    text = new TextWithSubViews("`hello ´`{   }´ world");

    makeCase(
      text,
      CmlParseError.EMPTY_INTERPOLATION,
      text.subView(1).startInclusive,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.FIRST_TEXT), "hello ")
    );
  }

  @Test
  public void shouldParseLiterals() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´ `b´=`false´ `c´=`null´>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new BooleanAttributeEvent(text.subView(1), text.subView(2), false, "b", "false"),
      new NullAttributeEvent(text.subView(3), text.subView(4), "c", "null"),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldThrowOnMissingAttributeValue() {
    TextWithSubViews text = new TextWithSubViews("<`red´ `a´=>");

    makeCase(
      text,
      CmlParseError.MISSING_ATTRIBUTE_VALUE,
      text.subView(1).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red")
    );

    text = new TextWithSubViews("<`red´ `a´=   >");

    makeCase(
      text,
      CmlParseError.MISSING_ATTRIBUTE_VALUE,
      text.subView(1).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red")
    );

    text = new TextWithSubViews("<`red´ `a´= <red>>");

    makeCase(
      text,
      CmlParseError.MISSING_ATTRIBUTE_VALUE,
      text.subView(1).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red")
    );
  }

  @Test
  public void shouldParseBothSingleQuotedAndDoubleQuotedStringAttributeValues() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´ `a´='`hello `\\´' \\\" world´' `b´=\"`hello \\' `\\´\" world´\">"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(
        text.subView(1),
        text.subView(2).addIndexToBeRemoved(text.subView(3).startInclusive),
        "a", "hello ' \\\" world"
      ),
      new StringAttributeEvent(
        text.subView(4),
        text.subView(5).addIndexToBeRemoved(text.subView(6).startInclusive),
        "b", "hello \\' \" world"
      ),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseTemplateLiteralAttributeValues() {
    TextWithSubViews text = new TextWithSubViews(
      "<`red´ `a´=`×``hello ´{`world´}` \" ' :)!´×`´>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TemplateLiteralAttributeEvent(
        text.subView(1),
        ExpressionParserTests.templateLiteral(
          text.subView(2),
          text.subView(3),
          ExpressionParserTests.terminal("world", text.subView(4)),
          text.subView(5)
        ),
        "a"
      ),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  private void makeMalformedAttributeValueCase(CmlParseError expectedError, String valueExpression) {
    TextWithSubViews text = new TextWithSubViews("<`red´ a=`" + valueExpression + "´");

    makeCase(
      text,
      expectedError,
      text.subView(1).startInclusive,
      new TagOpenBeginEvent(text.subView(0), "red")
    );
  }

  private static void makeCase(TextWithSubViews input, CmlEvent... expectedEvents) {
    makeCase(input, null, -1, expectedEvents);
  }

  private static void makeCase(TextWithSubViews input, @Nullable CmlParseError expectedError, int expectedPosition, CmlEvent... expectedEvents) {
    CmlEventJoiner actualEventsJoiner = new CmlEventJoiner();

    CmlParseException thrownException = null;

    try {
      CmlEventParser.parse(InputView.of(input.text), actualEventsJoiner);
    } catch (CmlParseException exception) {
      thrownException = exception;
    }

    if (expectedError == null && thrownException != null)
      throw new IllegalStateException("Expected there to be no exception thrown, but encountered " + thrownException.error);

    if (expectedError != null && thrownException == null)
      throw new IllegalStateException("Expected there to be an error of " + expectedError + ", but encountered none");

    if (expectedError != null) {
      assertEquals(expectedError, thrownException.error, "Encountered mismatch on thrown error-types");
      assertEquals(expectedPosition, thrownException.position, "Encountered mismatch on thrown error-types");
    }

    StringBuilder expectedEventsString = new StringBuilder();

    for (int expectedEventIndex = 0; expectedEventIndex < expectedEvents.length; ++expectedEventIndex) {
      if (expectedEventIndex != 0)
        expectedEventsString.append('\n');

      CmlEvent expectedEvent = expectedEvents[expectedEventIndex];

      expectedEventsString.append(Jsonifier.jsonify(expectedEvent));
    }

    assertEquals(expectedEventsString.toString(), actualEventsJoiner.toString());
  }
}
