/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.util.InputView;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarkupInterpreterTests extends InterpreterTestsBase {

  @Test
  public void shouldRenderSimpleText() {
    TextWithSubViews text = new TextWithSubViews(
      "<red><bold>Hello, world! :)"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world! :)")
        .string("color", "red")
        .bool("bold", true)
    );
  }

  @Test
  public void shouldRenderIfElse() {
    TextWithSubViews text = new TextWithSubViews(
      "<italic>",
      "  <red *if=\"my_flag\">My flag is true!</red>",
      "  <blue *else>My flag is false!</blue>"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("my_flag", true),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "My flag is true!")
        .string("color", "red")
        .bool("italic", true)
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("my_flag", false),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "My flag is false!")
        .string("color", "blue")
        .bool("italic", true)
    );
  }

  @Test
  public void shouldRenderWhenMatching() {
    TextWithSubViews text = new TextWithSubViews(
      "<container *when=\"input\">",
      "  <red +is=\"A\">Case A</>",
      "  <green +is=\"B\">Case B</>",
      "  <container +is=\"null\" *when=\"other_input\">",
      "    <gold +is=\"C\">Nested case C</>",
      "    <yellow +is=\"D\">Nested case D</>",
      "  </>",
      "  <gray *other>Fallback Case</>",
      "</>"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("input", "a"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Case A")
        .string("color", "red")
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("input", "b"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Case B")
        .string("color", "green")
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("input", "null")
        .withVariable("other_input", "C"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Nested case C")
        .string("color", "gold")
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("input", "null")
        .withVariable("other_input", "D"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Nested case D")
        .string("color", "yellow")
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("input", "null")
        .withVariable("other_input", "E"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("input", "asd"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Fallback Case")
        .string("color", "gray")
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("input", null),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Fallback Case")
        .string("color", "gray")
    );
  }

  @Test
  public void shouldRenderInterpolationWithBinding() {
    TextWithSubViews text = new TextWithSubViews(
      "<red *let-my_var=\"my_prefix & my_name & my_suffix\">Hello, {my_var}"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("my_prefix", "prefix ")
        .withVariable("my_name", "Steve")
        .withVariable("my_suffix", " suffix"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, prefix Steve suffix")
        .string("color", "red")
    );
  }

  @Test
  public void shouldRenderForLoop() {
    makeForLoopCase(false);
    makeForLoopCase(true);
  }

  private void makeForLoopCase(boolean reversed) {
    TextWithSubViews text = new TextWithSubViews(
      "<red",
      "  *for-char=\"my_chars\"",
      "  *for-separator={ <aqua>separator }",
      reversed ? "*for-reversed" : "",
      "  *let-index=\"loop.index\"",
      ">",
      "  {char} at index {index}"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("my_chars", Arrays.asList("A", "S", "T")),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(container -> (
              container
                .string("text", "A at index 0")
                .string("color", "red")
            ))
            .object(interpolation -> (
              interpolation
                .string("text", "separator")
                .string("color", "aqua")
            ))
            .object(container -> (
              container
                .string("text", "S at index 1")
                .string("color", "red")
            ))
            .object(interpolation -> (
              interpolation
                .string("text", "separator")
                .string("color", "aqua")
            ))
            .object(container -> (
              container
                .string("text", "T at index 2")
                .string("color", "red")
            ))
            .reverse(reversed)
        ))
    );
  }

  @Test
  public void shouldRenderHoverText() {
    TextWithSubViews text = new TextWithSubViews(
      "<aqua><bold><hover-text value={<red>Hello, hover!}>Hover over me!"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hover over me!")
        .string("color", "aqua")
        .bool("bold", true)
        .object("hoverEvent", event -> (
          event
            .string("action", "show_text")
            .object("contents", contents -> (
              contents
                .string("text", "Hello, hover!")
                .string("color", "red")
            ))
        ))
    );
  }

  @Test
  public void shouldUpdateLetBindingOnLoopNode() {
    TextWithSubViews text = new TextWithSubViews(
      "<container *for=\"1..3\" *for-separator={<space/>} *let-number=\"loop.index + 1\">{number}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "1 2 3")
    );
  }

  @Test
  public void shouldHandleMultipleEvents() {
    makeCase(
      new TextWithSubViews(
        "<hover-text value={<&c>Hovered!}>",
        "  <open-url value='https://google.com'>",
        "    <&e>Hello, world!"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .string("color", "yellow")
        .object("clickEvent", event -> (
          event
            .string("action", "open_url")
            .string("value", "https://google.com")
        ))
        .object("hoverEvent", event -> (
          event
            .string("action", "show_text")
            .object("contents", contents -> (
              contents
                .string("text", "Hovered!")
                .string("color", "red")
            ))
        ))
    );
  }

  @Test
  public void shouldHandleNestedHoverEventsWithMultipleContents() {
    makeCase(
      new TextWithSubViews(
        "<hover-text value={<&c>Outer hover}>",
        "  This message will show the outer-hover,",
        "  <hover-text value={<&a>Inner hover}>",
        "    except this part, which displays the inner-hover",
        "  </>",
        "  but this part will continue the outer-hover again.",
        "</>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .object("hoverEvent", event -> (
          event
            .string("action", "show_text")
            .object("contents", contents -> (
              contents
                .string("text", "Outer hover")
                .string("color", "red")
            ))
        ))
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "This message will show the outer-hover,")
            ))
            .object(item -> (
              item
                .string("text", "except this part, which displays the inner-hover")
                .object("hoverEvent", event -> (
                  event
                    .string("action", "show_text")
                    .object("contents", contents -> (
                      contents
                        .string("text", "Inner hover")
                        .string("color", "green")
                    ))
                ))
            ))
            .object(item -> (
              item
                .string("text", "but this part will continue the outer-hover again.")
            ))
        ))
    );
  }

  @Test
  public void shouldHandleNestedHoverEventsWithSingleContent() {
    makeCase(
      new TextWithSubViews(
        "<hover-text value={<&c>Outer hover}>",
        "  <hover-text value={<&a>Inner hover}>",
        "    Hello, world!",
        "  </>",
        "</>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .object("hoverEvent", event -> (
          event
            .string("action", "show_text")
            .object("contents", contents -> (
              contents
                .string("text", "Inner hover")
                .string("color", "green")
            ))
        ))
    );
  }

  @Test
  public void shouldHandleNestedLoopIndexLetBindings() {
    makeCase(
      new TextWithSubViews(
        "<container *for='1..3' *let-i='loop.index' *for-separator={;}>",
        "  <container *for='1..3' *let-j='loop.index' *for-separator={,}>",
        "    {i}{j}"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "00,01,02;10,11,12;20,21,22")
    );
  }

  @Test
  public void shouldIterateMultiLineComplexDataStructure() {
    makeCase(
      new TextWithSubViews(
        "<container",
        "  *let-data='''",
        "    [",
        "      {",
        "        a: 'first',",
        "        b: 1",
        "      },",
        "      {",
        "        a: 'second',",
        "        b: 2",
        "      },",
        "      {",
        "        a: 'third',",
        "        b: 3",
        "      }",
        "    ]",
        "  '''",
        "  *for-entry='data'",
        "  *for-separator={;}",
        ">{entry.a}-{entry.b}"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "first-1;second-2;third-3")
    );
  }

  @Test
  public void shouldGenerateAGradient() {
    TextWithSubViews text = new TextWithSubViews(
      "<gradient color=\"red\" color=\"blue\">Hello, <bold>world</>!"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateARainbow() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow> I am the <b>coolest rainbow</b> on earth </>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateAFullyBoldRainbow() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow><b>I am the coolest rainbow on earth"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateAFullyBoldRainbowWithPartialItalic() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow><b>I am the coolest <i>rainbow</> on earth"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateARainbowOnAnInterpolation() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow> I am {\"an amazing rainbow!\"}"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateARainbowOnAUnitNode() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow>The following is a translation: <translate key='my.key'/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      "Translate"
    );

    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow>The following is a key: <key key='my.key'/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      "Key"
    );
  }

  @Test
  public void shouldSkipNonTextOnARainbow() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow skip-non-text>The following is a key: <key key='my.key'/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateARainbowOnAColoredInterpolation() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow override-colors> I am <&7>{\"an amazing rainbow!\"}"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateARainbowAndSkipAColoredInterpolation() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow> I am an amazing <&f>{\"rainbow\"}</>!"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT
    );
  }

  @Test
  public void shouldGenerateARainbowOnMultipleLines() {
    makeRecordedCase(
      new TextWithSubViews(
        "<rainbow override-colors>",
        "  <container *for='1..3' *for-separator={<br/>}>",
        "    <&7 *for='1..3' *for-separator={<&7>,<space/>}>Item #{1}.{2}</>",
        "    <&e *if='loop.index % 2 eq 0'>x</>"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE
    );
  }

  @Test
  public void shouldGenerateATransition() {
    makeRecordedCase(
      new TextWithSubViews(
        "<transition",
          "*for-i=\"0..5\"",
          "*for-separator={<br/>}",
          "color=\"red\"",
          "color=\"blue\"",
          "[phase]=\"i/5.0 * 100\"",
        ">This is a <gold>multi-line</> transition"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE
    );
  }

  @Test
  public void shouldDifferentiateBetweenIfAndUse() {
    TextWithSubViews text = new TextWithSubViews(
      "<red *if=\"a\" *use=\"b\">Hello, world!"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", true)
        .withVariable("b", true),
      SlotType.CHAT,
      "TrueTrue"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", true)
        .withVariable("b", false),
      SlotType.CHAT,
      "TrueFalse"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", false)
        .withVariable("b", true),
      SlotType.CHAT,
      "FalseTrue"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", false)
        .withVariable("b", false),
      SlotType.CHAT,
      "FalseFalse"
    );
  }

  @Test
  public void shouldSkipRainbowsOnUseIsFalse() {
    TextWithSubViews text = new TextWithSubViews(
      "<rainbow *use=\"a\">Hello, world!"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", true),
      SlotType.CHAT,
      "True"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", false),
      SlotType.CHAT,
      "False"
    );
  }

  @Test
  public void shouldSkipStyleTagAttributesOnUseIsFalse() {
    TextWithSubViews text = new TextWithSubViews(
      "<style *use=\"a\" bold italic underlined color=\"red\" font=\"my.font\">Hello, world!"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", true),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .bool("bold", true)
        .bool("italic", true)
        .bool("underlined", true)
        .string("color", "red")
        .string("font", "my.font")
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", false),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
    );
  }

  @Test
  public void shouldNotRepeatInheritedStyle() {
    TextWithSubViews text = new TextWithSubViews(
      "<red><bold>hello</><italic>world <red>test</>"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .string("color", "red")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .bool("bold", true)
                .string("text", "hello")
            ))
            .object(item -> (
              item
                .string("text", "world test")
                .bool("italic", true)
            ))
        ))
    );
  }

  @Test
  public void shouldResetUnwantedInheritedStyle() {
    TextWithSubViews text = new TextWithSubViews(
      "<red>",
      "  <bold>",
      "    I am bold and red!",
      "    <reset>",
      "      <red>I am just red!"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .string("color", "red")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .bool("bold", true)
                .string("text", "I am bold and red!")
            ))
            .object(item -> (
              item
                .string("text", "I am just red!")
            ))
        ))
    );
  }

  @Test
  public void shouldResetLoreStyle() {
    TextWithSubViews text = new TextWithSubViews(
      "<reset>Hello, world!"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .bool("italic", false)
        .string("color", "white")
    );
  }

  @Test
  public void shouldAlwaysUseNearestValues() {
    TextWithSubViews text = new TextWithSubViews(
      // No color/font is terminated, so they nest
      "<red><style font=\"a\">first line<br/>",
      "<green><style font=\"b\">second line<br/>",
      "<blue><style font=\"c\">third line"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(line -> (
          line
            .string("text", "first line")
            .string("font", "a")
            .string("color", "red")
        ))
        .object(line -> (
          line
            .string("text", "second line")
            .string("font", "b")
            .string("color", "green")
        ))
        .object(line -> (
          line
            .string("text", "third line")
            .string("font", "c")
            .string("color", "blue")
        ))
    );
  }

  @Test
  public void shouldNotAddUnnecessaryStyles() {
    makeCase(
      new TextWithSubViews(
        "<red><italic><green><italic>hello, world</green><blue><italic>test me out</blue>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .bool("italic", true)
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "hello, world")
                .string("color", "green")
            ))
            .object(item -> (
              item
                .string("text", "test me out")
                .string("color", "blue")
            ))
        ))
    );
  }

  @Test
  public void shouldJoinSubsequentTextsOfEqualStyle() {
    TextWithSubViews text = new TextWithSubViews(
      "<red *for=\"1..5\">A</red>"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "AAAAA")
        .string("color", "red")
    );
  }

  @Test
  public void shouldJoinSubsequentTexts() {
    TextWithSubViews text = new TextWithSubViews(
      // Non-effective styled passages should also be interpreted as raw text
      "<red>Hello, <style [color]=\"null\">{a}</style> and {b}!"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", "first")
        .withVariable("b", "second"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, first and second!")
        .string("color", "red")
    );
  }

  @Test
  public void shouldJoinSubsequentTextsInATransition() {
    TextWithSubViews text = new TextWithSubViews(
      "<transition color=\"red\" color=\"blue\">Hello {a} world {b}!"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", "first")
        .withVariable("b", "second"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello first world second!")
        .string("color", "red")
    );
  }

  @Test
  public void shouldInterpolateMarkupValues() {
    TextWithSubViews text = new TextWithSubViews(
      "<red>before</> {markup_value} <blue>after</> {scalar_value}"
    );

    MarkupNode node = MarkupParser.parse(
      InputView.of("<bold><gold>I am a markup-value!"),
      BuiltInTagRegistry.INSTANCE
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("markup_value", node)
        .withVariable("scalar_value", "Hello, world!"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "before")
                .string("color", "red")
            ))
            .object(item -> item.string("text", " "))
            .object(item -> (
              item
                .string("text", "I am a markup-value!")
                .string("color", "gold")
                .bool("bold", true)
            ))
            .object(item -> item.string("text", " "))
            .object(item -> (
              item
                .string("text", "after")
                .string("color", "blue")
            ))
            .object(item -> item.string("text", " Hello, world!"))
        ))
    );
  }

  @Test
  public void shouldEvaluateBoundMarkupAttributeExpressionsWithAndWithoutSpread() {
    TextWithSubViews text = new TextWithSubViews(
      "<translate key=\"my.key\" [with]=\"first_node\"/>"
    );

    MarkupNode firstNode = MarkupParser.parse(
      InputView.of("<bold><gold>I am the first!"),
      BuiltInTagRegistry.INSTANCE
    );

    JsonObjectBuilder firstNodeJson = new JsonObjectBuilder()
      .string("text", "I am the first!")
      .string("color", "gold")
      .bool("bold", true);

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("first_node", firstNode),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("translate", "my.key")
        .array("with", with -> (
          with.object(item -> firstNodeJson)
        ))
    );

    MarkupNode secondNode = MarkupParser.parse(
      InputView.of("<italic><red>I am the second!"),
      BuiltInTagRegistry.INSTANCE
    );

    JsonObjectBuilder secondNodeJson = new JsonObjectBuilder()
      .string("text", "I am the second!")
      .string("color", "red")
      .bool("italic", true);

    text = new TextWithSubViews(
      "<translate key=\"my.key\" [with]=\"first_node\" [with]=\"second_node\"/>"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("first_node", firstNode)
        .withVariable("second_node", secondNode),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("translate", "my.key")
        .array("with", with -> (
          with
            .object(item -> firstNodeJson)
            .object(item -> secondNodeJson)
        ))
    );

    text = new TextWithSubViews(
      "<translate key=\"my.key\" [...with]=\"[first_node, second_node]\"/>"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("first_node", firstNode)
        .withVariable("second_node", secondNode),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("translate", "my.key")
        .array("with", with -> (
          with
            .object(item -> firstNodeJson)
            .object(item -> secondNodeJson)
        ))
    );

    text = new TextWithSubViews(
      "<translate key=\"my.key\" [with]=\"[first_node, second_node]\"/>"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("first_node", firstNode)
        .withVariable("second_node", secondNode),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("translate", "my.key")
        .array("with", with -> (
          with
            .object(withItem -> (
              withItem
                .string("text", "")
                .array("extra", extra -> (
                  extra
                    .object(item -> firstNodeJson)
                    .object(item -> secondNodeJson)
                ))
            ))
        ))
    );
  }

  @Test
  public void shouldRenderMarkupLetBindings() {
    TextWithSubViews text = new TextWithSubViews(
      "<container",
      "  *let-spacer={ <dark_gray><st>{' ' ** 15} }",
      "  *let-line={ <red>Hello, world! }",
      ">",
      "  {line}<br/>",
      "  {spacer}<br/>",
      "  {line}<br/>",
      "  {spacer}<br/>",
      "  {line}"
    );

    JsonObjectBuilder spacer = new JsonObjectBuilder()
      .string("text", "               ")
      .string("color", "dark_gray")
      .bool("strikethrough", true);

    JsonObjectBuilder line = new JsonObjectBuilder()
      .string("text", "Hello, world!")
      .string("color", "red");

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(o -> line)
            .object(o -> o.string("text", "\n"))
            .object(o -> spacer)
            .object(o -> o.string("text", "\n"))
            .object(o -> line)
            .object(o -> o.string("text", "\n"))
            .object(o -> spacer)
            .object(o -> o.string("text", "\n"))
            .object(o -> line)
        ))
    );
  }

  @Test
  public void shouldAllowToUseLetBindingsOnUnpackedNode() {
    TextWithSubViews text = new TextWithSubViews(
      "<red *let-a=\"'a'\">{a ** 10}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "aaaaaaaaaa")
        .string("color", "red")
    );
  }

  @Test
  public void shouldAllowLiteralLetBindings() {
    TextWithSubViews text = new TextWithSubViews(
      "<container",
      "  +let-a=5",
      "  +let-b=\"hello\"",
      "  +let-c=-.23",
      ">{a} {b} {c}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "5 hello -.23")
    );
  }

  @Test
  public void shouldAllowBackwardsAccessOnLetBindings() {
    TextWithSubViews text = new TextWithSubViews(
      "<container",
      "  *let-a=5",
      "  *let-b=12",
      "  *let-c=\"b - a\"",
      "  *let-d=3",
      "  *let-e=\"c ^ d\"",
      ">{e}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "343")
    );
  }

  @Test
  public void shouldResetInMultiComponentScenario() {
    TextWithSubViews text = new TextWithSubViews(
      "<reset>",
      "  <gray>",
      "    <translate key=\"a\"/> A<br/>",
      "    <translate key=\"b\"/> B<br/>",
      "    <translate key=\"c\"/> C",
      "  </gray>",
      "  <br/>",
      "  <translate key=\"d\"/> D<br/>",
      "  <red><translate key=\"e\"/> E",
      "</reset>",
      "<br/>",
      "last line"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(line -> (
          line
            .string("text", "")
            .string("color", "gray")
            .bool("italic", false)
            .array("extra", extra -> (
              extra
                .object(item -> item.string("translate", "a"))
                .object(item -> item.string("text", " A"))
            ))
        ))
        .object(line -> (
          line
            .string("text", "")
            .string("color", "gray")
            .bool("italic", false)
            .array("extra", extra -> (
              extra
                .object(item -> item.string("translate", "b"))
                .object(item -> item.string("text", " B"))
            ))
        ))
        .object(line -> (
          line
            .string("text", "")
            .string("color", "gray")
            .bool("italic", false)
            .array("extra", extra -> (
              extra
                .object(item -> item.string("translate", "c"))
                .object(item -> item.string("text", " C"))
            ))
        ))
        .object(line -> (
          line
            .string("text", "")
            .string("color", "white")
            .bool("italic", false)
            .array("extra", extra -> (
              extra
                .object(item -> item.string("translate", "d"))
                .object(item -> item.string("text", " D"))
            ))
        ))
        .object(line -> (
          line
            .string("text", "")
            .string("color", "red")
            .bool("italic", false)
            .array("extra", extra -> (
              extra
                .object(item -> item.string("translate", "e"))
                .object(item -> item.string("text", " E"))
            ))
        ))
        .object(line -> (
          line
            .string("text", "last line")
        ))
    );
  }

  @Test
  public void shouldEmitEmptyComponentsOnBackToBackBreaks() {
    TextWithSubViews text = new TextWithSubViews(
      "<red>First line</>",
      "<br/>",
      "<br/>",
      "<aqua>Last line</>"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(line -> (
          line
            .string("text", "First line")
            .string("color", "red")
        ))
        .object(line -> (
          line.string("text", "")
        ))
        .object(line -> (
          line
            .string("text", "Last line")
            .string("color", "aqua")
        ))
    );
  }

  @Test
  public void shouldNotHoistUpColorIfNotAllTextsAreColored() {
    // There was a bug where it would hoist up "gray" to the root
    // component, because it thought all text-members were gray.

    TextWithSubViews text = new TextWithSubViews(
      "<gray>Hello, world!</> :)"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "Hello, world!")
                .string("color", "gray")
            ))
            .object(item -> (
              item
                .string("text", " :)")
            ))
        ))
    );
  }

  @Test
  public void shouldCaptureVariablesOnDirectMarkupLetBinding() {
    TextWithSubViews text = new TextWithSubViews(
      "<container",
      "  +let-a=\"first\"",
      ">",
      "  <container",
      "    *let-(my_template)={ {a} and {b} }",
      "  >",
      "    <container",
      "      +let-a=\"third\"",
      "      +let-b=\"second\"",
      "    >{my_template}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "first and second")
    );
  }

  @Test
  public void shouldCaptureVariablesOnIndirectMarkupLetBinding() {
    TextWithSubViews text = new TextWithSubViews(
      "<container",
      "  +let-a=\"first\"",
      ">",
      "  <container",
      "    *let-my_template={ {a} and {b} }",
      "  >",
      "    <container",
      "      *let-(my_captured_template)=\"my_template\"",
      "    >",
      "      <container",
      "        +let-a=\"third\"",
      "        +let-b=\"second\"",
      "      >{my_template} | {my_captured_template}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "third and second | first and second")
    );
  }

  @Test
  public void shouldLoopAParameterizedRange() {
    TextWithSubViews text = new TextWithSubViews(
      "<container",
      "  *let-a=4",
      "  *let-b=6",
      "  *for-i=\"a..b\"",
      ">{i}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "456")
    );
  }

  @Test
  public void shouldRenderLetBindingComponent() {
    TextWithSubViews text = new TextWithSubViews(
      "<container",
      "  *let-spacer={",
      "    <dark_gray><st>{ ' ' ** 15 }",
      "  }",
      ">",
      "  {spacer}<br/>",
      "  <aqua>First line!</><br/>",
      "  {spacer}<br/>",
      "  <aqua>Second line!</><br/>",
      "  {spacer}<br/>",
      "  <aqua>Third line!</><br/>",
      "  {spacer}",
      "</>"
    );

    makeRecordedCase(
      text,
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE
    );
  }

  @Test
  public void shouldProcessTemplateLiteralValuesProperly() {
    TextWithSubViews text = new TextWithSubViews(
      "<translate *for=\"1..2\" *for-separator=×`{b}X{a}×` key=×`{a} middle {b}×` />"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", "first")
        .withVariable("b", "second"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("translate", "first middle second")
            ))
            .object(item -> (
              item
                .string("text", "secondXfirst")
            ))
            .object(item -> (
              item
                .string("translate", "first middle second")
            ))
        ))
    );
  }

  @Test
  public void shouldBeAbleToIterateAMap() {
    TextWithSubViews text = new TextWithSubViews(
      "<container *for-entry=\"my_map\">{entry}{my_map[entry]}"
    );

    Map<String, String> myMap = new HashMap<>();

    myMap.put("A", "1");
    myMap.put("B", "2");
    myMap.put("C", "3");

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("my_map", myMap),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "A1B2C3")
    );
  }

  @Test
  public void shouldOnlyAppendForSeparatorWhenIterationWasSuccessful() {
    TextWithSubViews text = new TextWithSubViews(
      "<red",
      "  *for-name=\"['A', 'B', 'C', 'D']\"",
      "  *for-separator={<red>,<space/>}",
      "  *if=\"name neq 'B'\"",
      ">#{loop.index + 1} {name}"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("color", "red")
        .string("text", "#1 A, #3 C, #4 D")
    );
  }

  @Test
  public void shouldApplyAllUppercaseHexColors() {
    TextWithSubViews text = new TextWithSubViews(
      "<#E8871E>Hello, world"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world")
        .string("color", "#E8871E")
    );
  }

  @Test
  public void shouldHandlePrefixOperatorOnMemberAccess() {
    TextWithSubViews text = new TextWithSubViews(
      "{len(lut.textures)}"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("lut", Collections.singletonMap("textures", Arrays.asList("a", "b", "c"))),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "3")
    );
  }

  @Test
  public void shouldRenderOutsideStyleThroughEvents() {
    TextWithSubViews text = new TextWithSubViews(
      "<&7>Aktuell aktives Prädikat:<space/>",
      "  <u>",
      "    <hover-text value={<&a>Klicke auf das Prädikat, um einen Editierungsbefehl zu erhalten.}>",
      "      <suggest-command value=×`{set_command}×`>",
      "        <&a>{predicate}",
      "  </u>",
      "<&7>, Sprache: <&a>{predicate_language}"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("set_command", "/test")
        .withVariable("predicate", "my predicate")
        .withVariable("predicate_language", "ENGLISH_US"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "Aktuell aktives Prädikat: ")
                .string("color", "gray")
            ))
            .object(item -> (
              item
                .string("text", "my predicate")
                .string("color", "green")
                .bool("underlined", true)
                .object("clickEvent", event -> (
                  event
                    .string("action", "suggest_command")
                    .string("value", "/test")
                ))
                .object("hoverEvent", event -> (
                  event
                    .string("action", "show_text")
                    .object("contents", contents -> (
                      contents
                        .string("text", "Klicke auf das Prädikat, um einen Editierungsbefehl zu erhalten.")
                        .string("color", "green")
                    ))
                ))
            ))
            .object(item -> (
              item
                .string("text", ", Sprache: ")
                .string("color", "gray")
            ))
            .object(item -> (
              item
                .string("text", "ENGLISH_US")
                .string("color", "green")
            ))
        ))
    );
  }

  @Test
  public void shouldToggleColorWithUseAndApplyFallbackSingleMember() {
    TextWithSubViews text = new TextWithSubViews(
      // Let's act as if there's still an open color from somewhere up above in more
      // complex input, as is often the case - the <&7> down below should override it.
      "<&c>",
      "  <container *for-name=\"names\" *for-separator={<br/>}>",
      "    <&7><&e *use=\"name eq 'second'\">{name}"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("names", Arrays.asList("first", "second", "third")),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(item -> (
          item
            .string("text", "first")
            .string("color", "gray")
        ))
        .object(item -> (
          item
            .string("text", "second")
            .string("color", "yellow")
        ))
        .object(item -> (
          item
            .string("text", "third")
            .string("color", "gray")
        ))
    );
  }

  @Test
  public void shouldToggleColorWithUseAndApplyFallbackMultiMember() {
    TextWithSubViews text = new TextWithSubViews(
      "<&c>",
      "  <container *for-name=\"names\" *for-separator={<br/>}>",
      "    <&7><&e *use=\"name eq 'second'\">#{loop.index + 1} {name}"
    );

    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("names", Arrays.asList("first", "second", "third")),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(item -> (
          item
            .string("text", "#1 first")
            .string("color", "gray")
        ))
        .object(item -> (
          item
            .string("text", "#2 second")
            .string("color", "yellow")
        ))
        .object(item -> (
          item
            .string("text", "#3 third")
            .string("color", "gray")
        ))
    );
  }

  @Test
  public void shouldBeAbleToBlockOutFormatting() {
    TextWithSubViews text = new TextWithSubViews(
      "<u>",
      "  I am underlined",
      "  <!u>; I am not</>",
      "  ; but I am again!"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "I am underlined")
                .bool("underlined", true)
            ))
            .object(item -> (
              item
                .string("text", "; I am not")
            ))
            .object(item -> (
              item
                .string("text", "; but I am again!")
                .bool("underlined", true)
            ))
        ))
    );
  }

  @Test
  public void shouldBeAbleToBlockOutFormattingImmediately() {
    TextWithSubViews text = new TextWithSubViews(
      "<u><!u>I am not underlined!"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "I am not underlined!")
    );
  }

  @Test
  public void shouldAllowToUseLetBindingsImmediatelyInAConditional() {
    makeLetBindingAndConditionalCase(new TextWithSubViews(
      "<container",
      "  *let-b='a + 5'",
      "  *if='b eq 6'",
      ">Hello, world!"
    ));

    makeLetBindingAndConditionalCase(new TextWithSubViews(
      "<container",
      "  *if='b eq 6'",
      "  *let-b='a + 5'",
      ">Hello, world!"
    ));

    makeLetBindingAndConditionalCase(new TextWithSubViews(
      "<container",
      "  *let-b='a + 5'",
      "  *if='b eq 6'",
      ">Hello, world!</container>",
      "<container *else>Bye, world!"
    ));

    makeLetBindingAndConditionalCase(new TextWithSubViews(
      "<container",
      "  *if='b eq 6'",
      "  *let-b='a + 5'",
      ">Hello, world!</container>",
      "<container *else>Bye, world!"
    ));
  }

  @Test
  public void shouldRenderValueOnSelfClosingContainerTag() {
    makeCase(
      new TextWithSubViews(
        "<container value={<red>Hello, world!} />"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .string("color", "red")
    );
  }

  @Test
  public void shouldRenderMultipleArbitraryValuesOnSelfClosingContainerTag() {
    makeCase(
      new TextWithSubViews(
        "<container",
        "  asd={<red>First}",
        "  bda={<green>Second}",
        "  aue={<blue>Third}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "First")
                .string("color", "red")
            ))
            .object(item -> (
              item
                .string("text", "Second")
                .string("color", "green")
            ))
            .object(item -> (
              item
                .string("text", "Third")
                .string("color", "blue")
            ))
        ))
    );
  }

  @Test
  public void shouldBindVariableAttributeByName() {
    makeCase(
      new TextWithSubViews(
        "<container",
        "  *let-asd={<red>Hello, world!}",
        "  &asd",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .string("color", "red")
    );
  }

  @Test
  public void shouldBindMemberAccessExpressionByInnermostName() {
    makeCase(
      new TextWithSubViews(
        "<key &keys.first.key />"
      ),
      new InterpretationEnvironment()
        .withVariable("keys", Collections.singletonMap("first", Collections.singletonMap("key", "my.key"))),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("keybind", "my.key")
    );
  }

  @Test
  public void shouldConvertUnderscoresToHyphensOnAttributesBoundByName() {
    makeCase(
      new TextWithSubViews(
        "<style",
        "  *let-shadow_opacity=50",
        "  &shadow_opacity",
        ">Hello, world!"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .string("shadow_color", "#00000080")
    );
  }

  @Test
  public void shouldIntroduceLetBindingsInheritedToAForLoopNode() {
    makeCase(
      new TextWithSubViews(
        "<container",
        "  +let-name_a='Notch'",
        "  +let-color_a='red'",
        "  +let-name_b='Steve'",
        "  +let-color_b='green'",
        "  +let-name_c='Alex'",
        "  +let-color_c='blue'",
        ">",
        "  <style",
        "    *for-x=\"['a', 'b', 'c']\"",
        "    *for-separator={<br/>}",
        "    [color]='env(×`color_{x}×`)'",
        "  >",
        "    {env(×`name_{x}×`)}"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(item -> (
          item
            .string("text", "Notch")
            .string("color", "red")
        ))
        .object(item -> (
          item
            .string("text", "Steve")
            .string("color", "green")
        ))
        .object(item -> (
          item
            .string("text", "Alex")
            .string("color", "blue")
        ))
    );
  }

  private void makeLetBindingAndConditionalCase(TextWithSubViews text) {
    makeCase(
      text,
      new InterpretationEnvironment()
        .withVariable("a", 1),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
    );
  }
}
