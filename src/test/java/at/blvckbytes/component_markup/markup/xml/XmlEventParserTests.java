package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.Jsonifier;
import at.blvckbytes.component_markup.markup.xml.event.*;
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
      "@<red@>@Hello, world! :)"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello, world! :)"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseAttributesOpeningWithContent() {
    TextWithAnchors text = new TextWithAnchors(
      "  @<red @attr-1=\"#string\" @attr-2=true @attr-3=false @attr-4=.3 @attr-5=-3@>@ my content"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new StringAttributeEvent("attr-1", text.auxAnchor(0), "string"),
      new BooleanAttributeEvent("attr-2", "true", true),
      new BooleanAttributeEvent("attr-3", "false", false),
      new DoubleAttributeEvent("attr-4", ".3", .3),
      new LongAttributeEvent("attr-5", "-3", -3),
      new TagOpenEndEvent("red", false),
      new TextEvent(" my content"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseMultilineAttributesOpeningWithContent() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red",
      "  @attr-1=\"#value 1\"",
      "  @attr-2=\"#value 2\"",
      "  @attr-3=\"#value 3\"",
      "@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new StringAttributeEvent("attr-1", text.auxAnchor(0), "value 1"),
      new StringAttributeEvent("attr-2", text.auxAnchor(1), "value 2"),
      new StringAttributeEvent("attr-3", text.auxAnchor(2), "value 3"),
      new TagOpenEndEvent("red", false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseSelfClosingTag() {
    TextWithAnchors text = new TextWithAnchors(
      "@<br /@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("br"),
      new TagOpenEndEvent("br", true),
      new InputEndEvent()
    );

    text = new TextWithAnchors(
      "@<br/@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("br"),
      new TagOpenEndEvent("br", true),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseOpeningAndClosingTagWithText() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello@</red>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello"),
      new TagCloseEvent("red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseIndentedTags() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>",
      "  @<bold@>@hi"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TagOpenBeginEvent("bold"),
      new TagOpenEndEvent("bold", false),
      new TextEvent("hi"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldRemoveTrailingWhitespace() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello ",
      "world ",
      "test@<bold@>",
      "@test2"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Helloworldtest"),
      new TagOpenBeginEvent("bold"),
      new TagOpenEndEvent("bold", false),
      new TextEvent("test2"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseMarkupAttribute() {
    TextWithAnchors text = new TextWithAnchors(
      "@<tag-outer",
      "  @attr-1={",
      "    @<red@>@Hello curly \\} bracket@</red>",
      "  @}",
      "@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("tag-outer"),
      new TagAttributeBeginEvent("attr-1"),
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello curly } bracket"),
      new TagCloseEvent("red"),
      new TagAttributeEndEvent("attr-1"),
      new TagOpenEndEvent("tag-outer", false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseInterpolationExpressions() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello, @{#user.name}@!"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello, "),
      new InterpolationEvent("user.name", text.auxAnchor(0)),
      new TextEvent("!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseFairlyComplexExpression() {
    TextWithAnchors text = new TextWithAnchors(
      "@<show-item",
      "  @name={@<red@>@My item!@</red>@}",
      "  @lore={@<blue@>@First line@<br/@>",
      "       @<green@>@Second line@<br/@>",
      "       @<gray",
      "         @*for-member=\"#members\"",
      "         @limit=5",
      "         @separator={@<br/@>@}",
      "         @empty={@<red@>@No items found!@}",
      "       @>@- @<yellow@>@{# member.item }@</gray>@<br/@>",
      "       @<gray@>@Last line! :)",
      "  @}",
      "@>@hover over @{#\"me\"}@! :)"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("show-item"),
      new TagAttributeBeginEvent("name"),
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("My item!"),
      new TagCloseEvent("red"),
      new TagAttributeEndEvent("name"),
      new TagAttributeBeginEvent("lore"),
      new TagOpenBeginEvent("blue"),
      new TagOpenEndEvent("blue", false),
      new TextEvent("First line"),
      new TagOpenBeginEvent("br"),
      new TagOpenEndEvent("br", true),
      new TagOpenBeginEvent("green"),
      new TagOpenEndEvent("green", false),
      new TextEvent("Second line"),
      new TagOpenBeginEvent("br"),
      new TagOpenEndEvent("br", true),
      new TagOpenBeginEvent("gray"),
      new StringAttributeEvent("*for-member", text.auxAnchor(0), "members"),
      new LongAttributeEvent("limit", "5", 5),
      new TagAttributeBeginEvent("separator"),
      new TagOpenBeginEvent("br"),
      new TagOpenEndEvent("br", true),
      new TagAttributeEndEvent("separator"),
      new TagAttributeBeginEvent("empty"),
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("No items found!"),
      new TagAttributeEndEvent("empty"),
      new TagOpenEndEvent("gray", false),
      new TextEvent("- "),
      new TagOpenBeginEvent("yellow"),
      new TagOpenEndEvent("yellow", false),
      new InterpolationEvent(" member.item ", text.auxAnchor(1)),
      new TagCloseEvent("gray"),
      new TagOpenBeginEvent("br"),
      new TagOpenEndEvent("br", true),
      new TagOpenBeginEvent("gray"),
      new TagOpenEndEvent("gray", false),
      new TextEvent("Last line! :)"),
      new TagAttributeEndEvent("lore"),
      new TagOpenEndEvent("show-item", false),
      new TextEvent("hover over "),
      new InterpolationEvent("\"me\"", text.auxAnchor(2)),
      new TextEvent("! :)"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseInterpolationWithCurlyBracketsInStrings() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello, @{#user.name + \"}\" + '}'}@!"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello, "),
      new InterpolationEvent("user.name + \"}\" + '}'", text.auxAnchor(0)),
      new TextEvent("!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldHandleAllTypesOfTextLocations() {
    TextWithAnchors text = new TextWithAnchors(
      "@  abcde @<red@>@ hello @<blue@>@ world!  "
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TextEvent("abcde "),
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent(" hello "),
      new TagOpenBeginEvent("blue"),
      new TagOpenEndEvent("blue", false),
      new TextEvent(" world!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldStripTrailingSpaces() {
    TextWithAnchors text = new TextWithAnchors(
      "@Online players:   ",
      "@<red@>@test"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TextEvent("Online players:"),
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("test"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldRemoveNewlineTrailingSpaces() {
    TextWithAnchors text = new TextWithAnchors(
      "@  hello",
      "    world",
      "   test"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TextEvent("helloworldtest"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveSurroundingContentSpaces() {
    TextWithAnchors text = new TextWithAnchors(
      "@<bold@>",
      "  @<red@>@  surrounding spaces  @</red>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("bold"),
      new TagOpenEndEvent("bold", false),
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("  surrounding spaces  "),
      new TagCloseEvent("red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveSurroundingInterpolationSpaces() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello @{#user.name}@ world!"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello "),
      new InterpolationEvent("user.name", text.auxAnchor(0)),
      new TextEvent(" world!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeCharactersInAttributeValues() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red @a=\"#hello \\\" quote\"@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new StringAttributeEvent("a", text.auxAnchor(0), "hello \" quote"),
      new TagOpenEndEvent("red", false),
      new InputEndEvent()
    );

    text = new TextWithAnchors(
      "@<red @a=\"#these > should < not require escaping\"@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new StringAttributeEvent("a", text.auxAnchor(0), "these > should < not require escaping"),
      new TagOpenEndEvent("red", false),
      new InputEndEvent()
    );

    text = new TextWithAnchors(
      "@<red @a={",
      "  @<green @b=\"#neither } should { these\"@>",
      "@}@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagAttributeBeginEvent("a"),
      new TagOpenBeginEvent("green"),
      new StringAttributeEvent("b", text.auxAnchor(0), "neither } should { these"),
      new TagOpenEndEvent("green", false),
      new TagAttributeEndEvent("a"),
      new TagOpenEndEvent("red", false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeLeadingCharacterInText() {
    TextWithAnchors text = new TextWithAnchors(
      "@\\<hello, world!"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TextEvent("<hello, world!"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldEscapeCharactersInText() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@' escaping \" closing \\> opening \\<; closing \\} opening \\{ \" and '@</red>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      // Within text-content, there's no need to escape quotes, as strings only occur
      // at values of attributes; also, there's no need to escape closing pointy-brackets,
      // as the predecessor tag (if any) is already closed.
      // I am >not< looking for general "consistency" here, but rather want to keep it as
      // syntactically terse as possible, while maintaining readability - thus, only escape
      // what's absolutely required as to avoid ambiguity while parsing.
      new TextEvent("' escaping \" closing \\> opening <; closing } opening { \" and '"),
      new TagCloseEvent("red"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldPreserveNonEscapingBackslashes() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red @a=\"#a \\ backslash\"@>@another \\ backslash"
    );

    // Because why not? :) There's absolutely no reason to treat backslashes which do not
    // actually escape critical characters any differently. I do not care about being "compatible"
    // with some general syntax out there, because I'm building a DSL!

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new StringAttributeEvent("a", text.auxAnchor(0), "a \\ backslash"),
      new TagOpenEndEvent("red", false),
      new TextEvent("another \\ backslash"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseFlagAttributes() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red @a @b@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new FlagAttributeEvent("a"),
      new FlagAttributeEvent("b"),
      new TagOpenEndEvent("red", false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldConsumeComments() {
    TextWithAnchors text = new TextWithAnchors(
      "<!-- My comment! :) -->"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new InputEndEvent()
    );

    text = new TextWithAnchors(
      "<!-- A shiny new container! -->",
      "@<container@>",
      "  <!-- An indented comment, :) -->",
      "  @<red@>@Hello, world! <!-- Trailing comment -->",
      "<!-- Trailing comment -->"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("container"),
      new TagOpenEndEvent("container", false),
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello, world! "),
      new InputEndEvent()
    );
  }

  // ================================================================================
  // Exception tests
  // ================================================================================

  @Test
  public void shouldThrowOnUnterminatedOpeningTag() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red"
    );

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.UNTERMINATED_TAG,
      new TagOpenBeginEvent("red")
    );
  }

  @Test
  public void shouldThrowOnUnterminatedClosingTag() {
    TextWithAnchors text = new TextWithAnchors(
      "@</red"
    );

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.UNTERMINATED_TAG,
      text.anchorEvent(0)
    );
  }

  @Test
  public void shouldThrowOnUnterminatedMarkupValue() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red",
      "  @my-attr=#{",
      "    @<green@>@Hello, \\} world!",
      ">"
    );

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.UNTERMINATED_MARKUP_VALUE,
      new TagOpenBeginEvent("red"),
      new TagAttributeBeginEvent("my-attr"),
      new TagOpenBeginEvent("green"),
      new TagOpenEndEvent("green", false),
      new TextEvent("Hello, } world!>"),
      text.auxAnchorEvent(0)
    );
  }

  @Test
  public void shouldThrowOnUnterminatedString() {
    makeMalformedAttributeValueCase(XmlParseError.UNTERMINATED_STRING, "\"hello world");
  }

  @Test
  public void shouldThrowOnUnterminatedInterpolation() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@{user.name + \"}\" + '}'"
    );

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      text.anchorEvent(2)
    );

    text = new TextWithAnchors(
      "@<red@>@{user.name\n}"
    );

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      text.anchorEvent(2)
    );

    text = new TextWithAnchors(
      "@<red@>@{user.name{"
    );

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      text.anchorEvent(2)
    );

    text = new TextWithAnchors(
      "@<red@>@{{"
    );

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.UNTERMINATED_INTERPOLATION,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      text.anchorEvent(2)
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
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_NUMBER, "5AB");
  }

  @Test
  public void shouldThrowOnMalformedLiterals() {
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_TRUE, "t");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_TRUE, "tr");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_TRUE, "tru");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_TRUE, "truea");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_FALSE, "f");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_FALSE, "fa");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_FALSE, "fal");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_FALSE, "fals");
    makeMalformedAttributeValueCase(XmlParseError.MALFORMED_LITERAL_FALSE, "falsea");
  }

  @Test
  public void shouldThrowOnUnsupportedAttributeValues() {
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "abc");
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "`test`");
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "<red>");
    makeMalformedAttributeValueCase(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "'test'");
  }

  @Test
  public void shouldThrowOnMissingTagName() {
    TextWithAnchors text = new TextWithAnchors("@<>");

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.MISSING_TAG_NAME,
      text.anchorEvent(0)
    );
  }

  @Test
  public void shouldAllowNullNamedClosingTag() {
    TextWithAnchors text = new TextWithAnchors("@</>");

    makeCaseWithInterleavedAnchors(
      text,
      new TagCloseEvent(null),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldThrowOnMalformedAttributeKeys() {
    TextWithAnchors text = new TextWithAnchors("@<red @my-attr @true>");

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.EXPECTED_ATTRIBUTE_KEY,
      new TagOpenBeginEvent("red"),
      new FlagAttributeEvent("my-attr"),
      text.anchorEvent(2)
    );

    text = new TextWithAnchors("@<red @my-attr @5var>");

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.EXPECTED_ATTRIBUTE_KEY,
      new TagOpenBeginEvent("red"),
      new FlagAttributeEvent("my-attr"),
      text.anchorEvent(2)
    );

    text = new TextWithAnchors("@<red @my-attr @\"my-string\">");

    makeCaseWithInterleavedAnchors(
      text,
      XmlParseError.EXPECTED_ATTRIBUTE_KEY,
      new TagOpenBeginEvent("red"),
      new FlagAttributeEvent("my-attr"),
      text.anchorEvent(2)
    );
  }

  @Test
  public void shouldThrowOnUnescapedCurlyBrackets() {
    makeCaseWithInterleavedAnchors(
      new TextWithAnchors("hello } world"),
      XmlParseError.UNESCAPED_CURLY
    );
  }

  private void makeMalformedAttributeValueCase(XmlParseError expectedError, String valueExpression) {
    TextWithAnchors text = new TextWithAnchors("@<red @a=" + valueExpression);

    makeCaseWithInterleavedAnchors(
      text,
      expectedError,
      new TagOpenBeginEvent("red"),
      text.anchorEvent(1)
    );
  }

  private static void makeCaseWithInterleavedAnchors(TextWithAnchors input, XmlEvent... expectedEvents) {
    makeCaseWithInterleavedAnchors(input, null, expectedEvents);
  }

  private static void makeCaseWithInterleavedAnchors(TextWithAnchors input, @Nullable XmlParseError expectedError, XmlEvent... expectedEvents) {
    XmlEventJoiner actualEventsJoiner = new XmlEventJoiner();

    XmlParseException thrownException = null;

    try {
      XmlEventParser.parse(input.text, actualEventsJoiner);
    } catch (XmlParseException exception) {
      thrownException = exception;
    }

    if (expectedError == null && thrownException != null)
      throw new IllegalStateException("Expected there to be no exception thrown, but encountered " + thrownException.error);

    if (expectedError != null && thrownException == null)
      throw new IllegalStateException("Expected there to be an error of " + expectedError + ", but encountered none");

    if (expectedError != null)
      assertEquals(expectedError, thrownException.error, "Encountered mismatch on thrown error-types");

    StringBuilder expectedEventsString = new StringBuilder();

    for (int expectedEventIndex = 0; expectedEventIndex < expectedEvents.length; ++expectedEventIndex) {
      if (expectedEventIndex != 0)
        expectedEventsString.append('\n');

      XmlEvent expectedEvent = expectedEvents[expectedEventIndex];

      if (!((expectedEvent instanceof InputEndEvent) || expectedEvent instanceof CursorPositionEvent)) {
        XmlEvent anchorEvent = input.anchorEvent(expectedEventIndex);

        if (anchorEvent == null)
          throw new IllegalStateException("Required " + (expectedEvents.length - 1) + " anchors, but only got " + input.getAnchorCount());

        expectedEventsString.append(Jsonifier.jsonify(anchorEvent));
        expectedEventsString.append('\n');
      }

      expectedEventsString.append(Jsonifier.jsonify(expectedEvent));
    }

    assertEquals(expectedEventsString.toString(), actualEventsJoiner.toString());
  }
}
