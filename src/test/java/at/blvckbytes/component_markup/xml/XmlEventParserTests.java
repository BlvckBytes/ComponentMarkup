package at.blvckbytes.component_markup.xml;

import at.blvckbytes.component_markup.xml.event.*;
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
      "  @<red @attr-1=\"string\" @attr-2=true @attr-3=false @attr-4=null @attr-5=.3 @attr-6=-3@>@ my content"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new StringAttributeEvent("attr-1", "string"),
      new BooleanAttributeEvent("attr-2", true),
      new BooleanAttributeEvent("attr-3", false),
      new NullAttributeEvent("attr-4"),
      new DoubleAttributeEvent("attr-5", .3),
      new LongAttributeEvent("attr-6", -3),
      new TagOpenEndEvent("red", false),
      new TextEvent(" my content"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseMultilineAttributesOpeningWithContent() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red",
      "  @attr-1=\"value 1\"",
      "  @attr-2=\"value 2\"",
      "  @attr-3=\"value 3\"",
      "@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new StringAttributeEvent("attr-1", "value 1"),
      new StringAttributeEvent("attr-2", "value 2"),
      new StringAttributeEvent("attr-3", "value 3"),
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
  public void shouldKeepTextWhitespace() {
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
      new TextEvent("Hello world test"),
      new TagOpenBeginEvent("bold"),
      new TagOpenEndEvent("bold", false),
      new TextEvent("test2"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseSubTreeAttribute() {
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
      "@<red@>@Hello, @{{user.name}}@!"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello, "),
      new InterpolationEvent("user.name"),
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
      "         @*for-member=\"members\"",
      "         @limit=5",
      "         @separator={@<br/@>@}",
      "         @empty={@<red@>@No items found!@}",
      "       @>@- @<yellow@>@{{ member.item }}@</gray>@<br/@>",
      "       @<gray@>@Last line! :)",
      "  @}",
      "@>@hover over @{{\"me\"}}@! :)"
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
      new StringAttributeEvent("*for-member", "members"),
      new LongAttributeEvent("limit", 5),
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
      new InterpolationEvent(" member.item "),
      new TagCloseEvent("gray"),
      new TagOpenBeginEvent("br"),
      new TagOpenEndEvent("br", true),
      new TagOpenBeginEvent("gray"),
      new TagOpenEndEvent("gray", false),
      new TextEvent("Last line! :)"),
      new TagAttributeEndEvent("lore"),
      new TagOpenEndEvent("show-item", false),
      new TextEvent("hover over "),
      new InterpolationEvent("\"me\""),
      new TextEvent("! :)"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseInterpolationWithCurlyBracketsInStrings() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello, @{{user.name + \"}}\" + '}}'}}@!"
    );

    makeCaseWithInterleavedAnchors(
      text,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      new TextEvent("Hello, "),
      new InterpolationEvent("user.name + \"}}\" + '}}'"),
      new TextEvent("!"),
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
      ParseError.UNTERMINATED_TAG,
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
      ParseError.UNTERMINATED_TAG,
      text.getAnchor(0)
    );
  }

  @Test
  public void shouldThrowOnUnterminatedSubtree() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red",
      "  @my-attr={",
      "    @<green@>@Hello, \\} world!",
      "@>"
    );

    makeCaseWithInterleavedAnchors(
      text,
      ParseError.UNTERMINATED_SUBTREE,
      new TagOpenBeginEvent("red"),
      new TagAttributeBeginEvent("my-attr"),
      new TagOpenBeginEvent("green"),
      new TagOpenEndEvent("green", false),
      new TextEvent("Hello, } world! >"),
      // TODO: Honestly, from a user-perspective, I kind of dislike the fact that this
      //       will always lie far beyond the origin-point; maybe emit the attr-cursor here?
      text.getAnchor(5)
    );
  }

  @Test
  public void shouldThrowOnUnterminatedString() {
    makeMalformedAttributeValueCase(ParseError.UNTERMINATED_STRING, "\"hello world");
  }

  @Test
  public void shouldThrowOnUnterminatedInterpolation() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@{{user.name + \"}}\" + '}}'"
    );

    makeCaseWithInterleavedAnchors(
      text,
      ParseError.UNTERMINATED_INTERPOLATION,
      new TagOpenBeginEvent("red"),
      new TagOpenEndEvent("red", false),
      text.getAnchor(2)
    );
  }

  @Test
  public void shouldThrowOnMalformedNumbers() {
    makeMalformedAttributeValueCase(ParseError.MALFORMED_NUMBER, ".");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_NUMBER, "-");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_NUMBER, "--");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_NUMBER, ".5.5");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_NUMBER, "5AB");
  }

  @Test
  public void shouldThrowOnMalformedLiterals() {
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_TRUE, "t");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_TRUE, "tr");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_TRUE, "tru");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_TRUE, "truea");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_FALSE, "f");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_FALSE, "fa");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_FALSE, "fal");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_FALSE, "fals");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_FALSE, "falsea");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_NULL, "n");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_NULL, "nu");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_NULL, "nul");
    makeMalformedAttributeValueCase(ParseError.MALFORMED_LITERAL_NULL, "nulla");
  }

  @Test
  public void shouldThrowOnUnsupportedAttributeValues() {
    makeMalformedAttributeValueCase(ParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "abc");
    makeMalformedAttributeValueCase(ParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "`test`");
    makeMalformedAttributeValueCase(ParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "<red>");
    makeMalformedAttributeValueCase(ParseError.UNSUPPORTED_ATTRIBUTE_VALUE, "'test'");
  }

  @Test
  public void shouldThrowOnMissingTagName() {
    TextWithAnchors text = new TextWithAnchors("@<>");

    makeCaseWithInterleavedAnchors(
      text,
      ParseError.MISSING_TAG_NAME,
      text.getAnchor(0)
    );

    text = new TextWithAnchors("@</>");

    makeCaseWithInterleavedAnchors(
      text,
      ParseError.MISSING_TAG_NAME,
      text.getAnchor(0)
    );
  }

  @Test
  public void shouldThrowOnMissingAttributeEquals() {
    TextWithAnchors text = new TextWithAnchors("@<red @my-attr true");

    makeCaseWithInterleavedAnchors(
      text,
      ParseError.MISSING_ATTRIBUTE_EQUALS,
      new TagOpenBeginEvent("red"),
      text.getAnchor(1)
    );

    text = new TextWithAnchors("@<red @my-attr>");

    makeCaseWithInterleavedAnchors(
      text,
      ParseError.MISSING_ATTRIBUTE_EQUALS,
      new TagOpenBeginEvent("red"),
      text.getAnchor(1)
    );
  }

  private void makeMalformedAttributeValueCase(ParseError expectedError, String valueExpression) {
    TextWithAnchors text = new TextWithAnchors("@<red @a=" + valueExpression);

    makeCaseWithInterleavedAnchors(
      text,
      expectedError,
      new TagOpenBeginEvent("red"),
      text.getAnchor(1)
    );
  }

  private static void makeCaseWithInterleavedAnchors(TextWithAnchors input, XmlEvent... expectedEvents) {
    makeCaseWithInterleavedAnchors(input, null, expectedEvents);
  }

  private static void makeCaseWithInterleavedAnchors(TextWithAnchors input, @Nullable ParseError expectedError, XmlEvent... expectedEvents) {
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

      if (!((expectedEvent instanceof InputEndEvent) || expectedEvent instanceof BeforeEventCursorEvent)) {
        XmlEvent anchorEvent = input.getAnchor(expectedEventIndex);

        if (anchorEvent == null)
          throw new IllegalStateException("Required " + (expectedEvents.length - 1) + " anchors, but only got " + input.getAnchorCount());

        expectedEventsString.append(anchorEvent);
        expectedEventsString.append('\n');
      }

      expectedEventsString.append(expectedEvent);
    }

    assertEquals(expectedEventsString.toString(), actualEventsJoiner.toString());
  }
}
