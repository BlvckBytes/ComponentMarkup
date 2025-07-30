package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.markup.xml.event.*;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class XmlEventParserTests {

  // ================================================================================
  // Event-sequence tests
  // ================================================================================

  @Test
  public void shouldParseNoAttributesOpeningWithContent() {
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "<  `br´ />"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "br"),
      new TagOpenEndEvent(text.subView(0), true),
      new InputEndEvent()
    );

    text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "<`red´>`Hello´@</`red´>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello"),
      new TagCloseEvent(text.subView(2), text.anchor(0), "red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseIndentedTags() {
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "<`tag-outer´",
      "  `attr-1´=@{",
      "    <`red´>`Hello curly @\\} bracket´@</`red´>",
      "  }",
      ">"
    );

    text.addViewIndexToBeRemoved(text.anchor(1));

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "tag-outer"),
      new TagAttributeBeginEvent(text.subView(1), text.anchor(0), "attr-1"),
      new TagOpenBeginEvent(text.subView(2), "red"),
      new TagOpenEndEvent(text.subView(2), false),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello curly } bracket"),
      new TagCloseEvent(text.subView(4), text.anchor(2), "red"),
      new TagAttributeEndEvent(text.subView(1)),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseInterpolationExpressions() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´>`Hello, ´{`user.name´}`!´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello, "),
      new InterpolationEvent(text.subView(2), "user.name"),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), "!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseFairlyComplexExpression() {
    TextWithAnchors text = new TextWithAnchors(
      "<`show-item´",
      "  `name´=@{<`red´>`My item!´@</`red´>}",
      "  `lore´=@{<`blue´>`First line´<`br´/>",
      "       <`green´>`Second line´<`br´/>",
      "       <`gray´",
      "         `*for-member´=\"`members´\"",
      "         `limit´=`5´",
      "         `separator´=@{<`br´/>}",
      "         `empty´=@{<`red´>`No items found!´}",
      "       >`- ´<`yellow´>{` member.item ´}@</`gray´><`br´/>",
      "       <`gray´>`Last line! :)",
      "  ´}",
      ">`hover over ´{`\"me\"´}`! ´<`red´ `my_flag´>`:)´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "show-item"),
      new TagAttributeBeginEvent(text.subView(1), text.anchor(0), "name"),
      new TagOpenBeginEvent(text.subView(2), "red"),
      new TagOpenEndEvent(text.subView(2), false),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.INNER_TEXT), "My item!"),
      new TagCloseEvent(text.subView(4), text.anchor(1), "red"),
      new TagAttributeEndEvent(text.subView(1)),
      new TagAttributeBeginEvent(text.subView(5), text.anchor(2), "lore"),
      new TagOpenBeginEvent(text.subView(6), "blue"),
      new TagOpenEndEvent(text.subView(6), false),
      new TextEvent(text.subView(7).setBuildFlags(SubstringFlag.INNER_TEXT), "First line"),
      new TagOpenBeginEvent(text.subView(8), "br"),
      new TagOpenEndEvent(text.subView(8), true),
      new TagOpenBeginEvent(text.subView(9), "green"),
      new TagOpenEndEvent(text.subView(9), false),
      new TextEvent(text.subView(10).setBuildFlags(SubstringFlag.INNER_TEXT), "Second line"),
      new TagOpenBeginEvent(text.subView(11), "br"),
      new TagOpenEndEvent(text.subView(11), true),
      new TagOpenBeginEvent(text.subView(12), "gray"),
      new StringAttributeEvent(text.subView(13), text.subView(14), "*for-member", "members"),
      new LongAttributeEvent(text.subView(15), text.subView(16), 5, "limit", "5"),
      new TagAttributeBeginEvent(text.subView(17), text.anchor(3), "separator"),
      new TagOpenBeginEvent(text.subView(18), "br"),
      new TagOpenEndEvent(text.subView(18), true),
      new TagAttributeEndEvent(text.subView(17)),
      new TagAttributeBeginEvent(text.subView(19), text.anchor(4), "empty"),
      new TagOpenBeginEvent(text.subView(20), "red"),
      new TagOpenEndEvent(text.subView(20), false),
      new TextEvent(text.subView(21).setBuildFlags(SubstringFlag.LAST_TEXT), "No items found!"),
      new TagAttributeEndEvent(text.subView(19)),
      new TagOpenEndEvent(text.subView(12), false),
      new TextEvent(text.subView(22).setBuildFlags(SubstringFlag.INNER_TEXT), "- "),
      new TagOpenBeginEvent(text.subView(23), "yellow"),
      new TagOpenEndEvent(text.subView(23), false),
      new InterpolationEvent(text.subView(24), " member.item "),
      new TagCloseEvent(text.subView(25), text.anchor(5), "gray"),
      new TagOpenBeginEvent(text.subView(26), "br"),
      new TagOpenEndEvent(text.subView(26), true),
      new TagOpenBeginEvent(text.subView(27), "gray"),
      new TagOpenEndEvent(text.subView(27), false),
      new TextEvent(text.subView(28).setBuildFlags(SubstringFlag.LAST_TEXT), "Last line! :)"),
      new TagAttributeEndEvent(text.subView(5)),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(29).setBuildFlags(SubstringFlag.INNER_TEXT), "hover over "),
      new InterpolationEvent(text.subView(30), "\"me\""),
      new TextEvent(text.subView(31).setBuildFlags(SubstringFlag.INNER_TEXT), "! "),
      new TagOpenBeginEvent(text.subView(32), "red"),
      new FlagAttributeEvent(text.subView(33), "my_flag"),
      new TagOpenEndEvent(text.subView(32), false),
      new TextEvent(text.subView(34).setBuildFlags(SubstringFlag.LAST_TEXT), ":)"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseInterpolationWithCurlyBracketsInStrings() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´>`Hello, ´{`user.name + \"}\" + '}'´}`!´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello, "),
      new InterpolationEvent(text.subView(2), "user.name + \"}\" + '}'"),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), "!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldHandleAllTypesOfTextLocations() {
    TextWithAnchors text = new TextWithAnchors(
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

    text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "<`bold´>",
      "  <`red´>`  surrounding spaces  ´@</`red´>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "bold"),
      new TagOpenEndEvent(text.subView(0), false),
      new TagOpenBeginEvent(text.subView(1), "red"),
      new TagOpenEndEvent(text.subView(1), false),
      new TextEvent(text.subView(2).setBuildFlags(SubstringFlag.INNER_TEXT), "  surrounding spaces  "),
      new TagCloseEvent(text.subView(3), text.anchor(0), "red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveSurroundingInterpolationSpaces() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´>`Hello ´{`user.name´}` world!´"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false),
      new TextEvent(text.subView(1).setBuildFlags(SubstringFlag.INNER_TEXT), "Hello "),
      new InterpolationEvent(text.subView(2), "user.name"),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), " world!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeCharactersInAttributeValues() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´ `a´=\"`hello @\\\" quote´\">"
    );

    text.addViewIndexToBeRemoved(text.anchor(0));

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(text.subView(1), text.subView(2), "a", "hello \" quote"),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );

    text = new TextWithAnchors(
      "<`red´ `a´=\"`these > should < not require escaping´\">"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new StringAttributeEvent(text.subView(1), text.subView(2), "a", "these > should < not require escaping"),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );

    text = new TextWithAnchors(
      "<`red´ `a´=@{",
      "  <`green´ `b´=\"`neither } should { these´\">",
      "}>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagAttributeBeginEvent(text.subView(1), text.anchor(0), "a"),
      new TagOpenBeginEvent(text.subView(2), "green"),
      new StringAttributeEvent(text.subView(3), text.subView(4), "b", "neither } should { these"),
      new TagOpenEndEvent(text.subView(2), false),
      new TagAttributeEndEvent(text.subView(1)),
      new TagOpenEndEvent(text.subView(0), false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeLeadingCharacterInText() {
    TextWithAnchors text = new TextWithAnchors(
      "`@\\<hello, world!´"
    );

    text.addViewIndexToBeRemoved(text.anchor(0));

    makeCase(
      text,
      new TextEvent(text.subView(0).setBuildFlags(SubstringFlag.ONLY_TEXT), "<hello, world!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeCharactersInText() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´>`' escaping \" closing \\> opening @\\<; closing @\\} opening @\\{ \" and '´@</`red´>"
    );

    text.addViewIndexToBeRemoved(text.anchor(0));
    text.addViewIndexToBeRemoved(text.anchor(1));
    text.addViewIndexToBeRemoved(text.anchor(2));

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
      new TagCloseEvent(text.subView(2), text.anchor(3), "red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveNonEscapingBackslashes() {
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "<!-- My comment! :) -->"
    );

    makeCase(
      text,
      new InputEndEvent()
    );

    text = new TextWithAnchors(
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
    TextWithAnchors text = new TextWithAnchors(
      "<`container´ `*let-test´=@{ {`a´}` and ´{`b´} }>"
    );

    makeCase(
      text,
      new TagOpenBeginEvent(text.subView(0), "container"),
      new TagAttributeBeginEvent(text.subView(1), text.anchor(0), "*let-test"),
      new InterpolationEvent(text.subView(2), "a"),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.INNER_TEXT), " and "),
      new InterpolationEvent(text.subView(4), "b"),
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
    TextWithAnchors text = new TextWithAnchors(
      "@<`red´"
    );

    makeCase(
      text,
      XmlParseError.UNTERMINATED_TAG,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red")
    );
  }

  @Test
  public void shouldThrowOnUnterminatedClosingTag() {
    TextWithAnchors text = new TextWithAnchors(
      "@</red"
    );

    makeCase(
      text,
      XmlParseError.UNTERMINATED_TAG,
      text.anchor(0)
    );
  }

  @Test
  public void shouldThrowOnUnterminatedMarkupValue() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´",
      "  `my-attr´=@{",
      "    <`green´>`Hello, @\\} world!",
      ">´"
    );

    text.addViewIndexToBeRemoved(text.anchor(1));

    makeCase(
      text,
      XmlParseError.UNTERMINATED_MARKUP_VALUE,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagAttributeBeginEvent(text.subView(1), text.anchor(0), "my-attr"),
      new TagOpenBeginEvent(text.subView(2), "green"),
      new TagOpenEndEvent(text.subView(2), false),
      new TextEvent(text.subView(3).setBuildFlags(SubstringFlag.LAST_TEXT), "Hello, } world!>")
    );
  }

  @Test
  public void shouldThrowOnUnterminatedString() {
    makeMalformedAttributeValueCase(XmlParseError.UNTERMINATED_STRING, "\"hello world");
  }

  @Test
  public void shouldThrowOnUnterminatedInterpolation() {
    TextWithAnchors text = new TextWithAnchors(
      "<`red´>@{user.name + \"}\" + '}'"
    );

    makeCase(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false)
    );

    text = new TextWithAnchors(
      "<`red´>@{user.name\n}"
    );

    makeCase(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false)
    );

    text = new TextWithAnchors(
      "<`red´>@{user.name{"
    );

    makeCase(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false)
    );

    text = new TextWithAnchors(
      "<`red´>@{{"
    );

    makeCase(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red"),
      new TagOpenEndEvent(text.subView(0), false)
    );
  }

  @Test
  public void shouldThrowOnLinebreakInString() {
    makeMalformedAttributeValueCase(XmlParseError.UNTERMINATED_STRING, "\"hello \n world\"");
    makeMalformedAttributeValueCase(XmlParseError.UNTERMINATED_STRING, "\"hello \r world\"");
  }

  @Test
  public void shouldThrowOnMalformedNumbers() {
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_NUMBER, ".");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_NUMBER, "-");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_NUMBER, "--");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_NUMBER, ".5.5");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_NUMBER, "- 5.5");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_NUMBER, "5AB");
  }

  @Test
  public void shouldThrowOnUnsupportedAttributeValues() {
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "abc");
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "\\`test\\`");
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "<red>");
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "'test'");
  }

  @Test
  public void shouldThrowOnMissingTagName() {
    TextWithAnchors text = new TextWithAnchors("@<>");
    makeCase(text, XmlParseError.MISSING_TAG_NAME, text.anchor(0));
  }

  @Test
  public void shouldAllowNullNamedClosingTag() {
    TextWithAnchors text = new TextWithAnchors("@</>");

    makeCase(
      text,
      new TagCloseEvent(null, text.anchor(0), null),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldThrowOnMalformedAttributeKeys() {
    TextWithAnchors text = new TextWithAnchors("<`red´ `my-attr´ @5var>");

    makeCase(
      text,
      XmlParseError.EXPECTED_ATTRIBUTE_KEY,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red"),
      new FlagAttributeEvent(text.subView(1), "my-attr")
    );

    text = new TextWithAnchors("<`red´ `my-attr´ @\"my-string\">");

    makeCase(
      text,
      XmlParseError.EXPECTED_ATTRIBUTE_KEY,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red"),
      new FlagAttributeEvent(text.subView(1), "my-attr")
    );
  }

  @Test
  public void shouldThrowOnUnescapedCurlyBrackets() {
    TextWithAnchors text = new TextWithAnchors("hello @} world");

    makeCase(
      text,
      XmlParseError.UNESCAPED_CURLY,
      text.anchor(0)
    );
  }

  @Test
  public void shouldThrowOnMalformedComments() {
    TextWithAnchors text = new TextWithAnchors("<@!-- Hello, world");
    makeCase(text, XmlParseError.MALFORMED_COMMENT, text.anchor(0));

    text = new TextWithAnchors("<@!-- Hello, world -");
    makeCase(text, XmlParseError.MALFORMED_COMMENT, text.anchor(0));

    text = new TextWithAnchors("<@!-- Hello, world ->");
    makeCase(text, XmlParseError.MALFORMED_COMMENT, text.anchor(0));

    text = new TextWithAnchors("<@!-- Hello, world --");
    makeCase(text, XmlParseError.MALFORMED_COMMENT, text.anchor(0));
  }

  private void makeMalformedAttributeValueCase(XmlParseError expectedError, String valueExpression) {
    TextWithAnchors text = new TextWithAnchors("<`red´ a=@" + valueExpression);

    makeCase(
      text,
      expectedError,
      text.anchor(0),
      new TagOpenBeginEvent(text.subView(0), "red")
    );
  }

  private static void makeCase(TextWithAnchors input, XmlEvent... expectedEvents) {
    makeCase(input, null, -1, expectedEvents);
  }

  private static void makeCase(TextWithAnchors input, @Nullable XmlParseError expectedError, int expectedPosition, XmlEvent... expectedEvents) {
    XmlEventJoiner actualEventsJoiner = new XmlEventJoiner();

    XmlParseException thrownException = null;

    try {
      XmlEventParser.parse(StringView.of(input.text), actualEventsJoiner);
    } catch (XmlParseException exception) {
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

      XmlEvent expectedEvent = expectedEvents[expectedEventIndex];

      expectedEventsString.append(Jsonifier.jsonify(expectedEvent));
    }

    assertEquals(expectedEventsString.toString(), actualEventsJoiner.toString());
  }
}
