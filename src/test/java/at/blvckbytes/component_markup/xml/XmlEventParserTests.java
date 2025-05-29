package at.blvckbytes.component_markup.xml;

import at.blvckbytes.component_markup.xml.event.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class XmlEventParserTests {

  /*
    Just a little sketch-board:

    <show-item
      name=<red>My item!</red>
      lore={<blue>First line<br/>
           <green>Second line<br/>
           <gray
             *for-member="members"
             limit=5
             separator={<br/>}
             empty={<red>No items found!}
           >- <yellow>{member.item}</gray><br/>
           <gray>Last line! :)
      }
    >hover over me! :)
   */

  @Test
  public void shouldParseNoAttributesOpeningWithContent() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello, world! :)"
    );

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("red"),
      text.getAnchor(1),
      new TagOpenEndEvent("red", false),
      text.getAnchor(2),
      new TextEvent("Hello, world! :)"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseAttributesOpeningWithContent() {
    TextWithAnchors text = new TextWithAnchors(
      "  @<red @attr-1=\"string\" @attr-2=true @attr-3=false @attr-4=null @attr-5=.3 @attr-6=-3@>@ my content"
    );

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("red"),
      text.getAnchor(1),
      new StringAttributeEvent("attr-1", "string"),
      text.getAnchor(2),
      new BooleanAttributeEvent("attr-2", true),
      text.getAnchor(3),
      new BooleanAttributeEvent("attr-3", false),
      text.getAnchor(4),
      new NullAttributeEvent("attr-4"),
      text.getAnchor(5),
      new DoubleAttributeEvent("attr-5", .3),
      text.getAnchor(6),
      new LongAttributeEvent("attr-6", -3),
      text.getAnchor(7),
      new TagOpenEndEvent("red", false),
      text.getAnchor(8),
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

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("red"),
      text.getAnchor(1),
      new StringAttributeEvent("attr-1", "value 1"),
      text.getAnchor(2),
      new StringAttributeEvent("attr-2", "value 2"),
      text.getAnchor(3),
      new StringAttributeEvent("attr-3", "value 3"),
      text.getAnchor(4),
      new TagOpenEndEvent("red", false),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseSelfClosingTag() {
    TextWithAnchors text = new TextWithAnchors(
      "@<br /@>"
    );

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("br"),
      text.getAnchor(1),
      new TagOpenEndEvent("br", true),
      new InputEndEvent()
    );

    text = new TextWithAnchors(
      "@<br/@>"
    );

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("br"),
      text.getAnchor(1),
      new TagOpenEndEvent("br", true),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseOpeningAndClosingTagWithText() {
    TextWithAnchors text = new TextWithAnchors(
      "@<red@>@Hello@</red>"
    );

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("red"),
      text.getAnchor(1),
      new TagOpenEndEvent("red", false),
      text.getAnchor(2),
      new TextEvent("Hello"),
      text.getAnchor(3),
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

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("red"),
      text.getAnchor(1),
      new TagOpenEndEvent("red", false),
      text.getAnchor(2),
      new TagOpenBeginEvent("bold"),
      text.getAnchor(3),
      new TagOpenEndEvent("bold", false),
      text.getAnchor(4),
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

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("red"),
      text.getAnchor(1),
      new TagOpenEndEvent("red", false),
      text.getAnchor(2),
      new TextEvent("Hello world test"),
      text.getAnchor(3),
      new TagOpenBeginEvent("bold"),
      text.getAnchor(4),
      new TagOpenEndEvent("bold", false),
      text.getAnchor(5),
      new TextEvent("test2"),
      new InputEndEvent()
    );
  }

  @Test
  public void shouldParseSubTreeAttribute() {
    TextWithAnchors text = new TextWithAnchors(
      "@<tag-outer",
      "  @attr-1={",
      "    @<red@>@Hello@</red>",
      "  @}",
      "@>"
    );

    makeCase(
      text,
      text.getAnchor(0),
      new TagOpenBeginEvent("tag-outer"),
      text.getAnchor(1),
      new TagAttributeBeginEvent("attr-1"),
      text.getAnchor(2),
      new TagOpenBeginEvent("red"),
      text.getAnchor(3),
      new TagOpenEndEvent("red", false),
      text.getAnchor(4),
      new TextEvent("Hello"),
      text.getAnchor(5),
      new TagCloseEvent("red"),
      text.getAnchor(6),
      new TagAttributeEndEvent("attr-1"),
      text.getAnchor(7),
      new TagOpenEndEvent("tag-outer", false),
      new InputEndEvent()
    );
  }

  private static void makeCase(TextWithAnchors input, XmlEvent... expectedEvents) {
    XmlEventJoiner actualEventsJoiner = new XmlEventJoiner();
    XmlEventParser.parse(input.text, actualEventsJoiner);

    StringBuilder expectedEventsString = new StringBuilder();

    for (XmlEvent expectedEvent : expectedEvents) {
      if (expectedEventsString.length() > 0)
        expectedEventsString.append('\n');

      expectedEventsString.append(expectedEvent);
    }

    assertEquals(expectedEventsString.toString(), actualEventsJoiner.toString());
  }
}
