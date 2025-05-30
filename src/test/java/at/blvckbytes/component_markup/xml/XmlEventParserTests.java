package at.blvckbytes.component_markup.xml;

import at.blvckbytes.component_markup.xml.event.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class XmlEventParserTests {

  // TODO: Test that cursors are emitted when errors are being thrown too, as proper
  //       error-messages couldn't be generated otherwise

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

  private static void makeCaseWithInterleavedAnchors(TextWithAnchors input, XmlEvent... expectedEvents) {
    XmlEventJoiner actualEventsJoiner = new XmlEventJoiner();
    XmlEventParser.parse(input.text, actualEventsJoiner);

    StringBuilder expectedEventsString = new StringBuilder();

    for (int expectedEventIndex = 0; expectedEventIndex < expectedEvents.length; ++expectedEventIndex) {
      if (expectedEventIndex != 0)
        expectedEventsString.append('\n');

      XmlEvent expectedEvent = expectedEvents[expectedEventIndex];

      if (!(expectedEvent instanceof InputEndEvent)) {
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
