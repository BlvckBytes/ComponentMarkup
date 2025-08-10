/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.platform.SlotType;
import at.blvckbytes.component_markup.util.StringView;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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
      "  *" + (reversed ? "" : "!") + "for-reversed",
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
        .bool("bold", true)
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "I am bold and red!")
            ))
            .object(item -> (
              item
                .string("text", "I am just red!")
                .bool("bold", false)
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
      StringView.of("<bold><gold>I am a markup-value!"),
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
      StringView.of("<bold><gold>I am the first!"),
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
      StringView.of("<italic><red>I am the second!"),
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
}
