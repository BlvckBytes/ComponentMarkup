package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.expression.interpreter.EnvironmentBuilder;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import com.google.gson.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class MarkupInterpreterTests {

  private static final Gson gsonInstance = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
  private static final ComponentConstructor componentConstructor = new JsonComponentConstructor();

  @Test
  public void shouldRenderSimpleText() {
    TextWithAnchors text = new TextWithAnchors(
      "<red><bold>Hello, world! :)"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world! :)")
        .string("color", "red")
        .bool("bold", true)
    );
  }

  @Test
  public void shouldRenderIfElse() {
    TextWithAnchors text = new TextWithAnchors(
      "<italic>",
      "  <red *if=\"my_flag\">My flag is true!</red>",
      "  <blue *else>My flag is false!</blue>"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("my_flag", true),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "My flag is true!")
        .string("color", "red")
        .bool("italic", true)
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("my_flag", false),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "My flag is false!")
        .string("color", "blue")
        .bool("italic", true)
    );
  }

  @Test
  public void shouldRenderWhenMatching() {
    TextWithAnchors text = new TextWithAnchors(
      "<container *when=\"input\">",
      "  <red *is=\"A\">Case A</>",
      "  <green *is=\"B\">Case B</>",
      "  <container *is=\"null\" *when=\"other_input\">",
      "    <gold *is=\"C\">Nested case C</>",
      "    <yellow *is=\"D\">Nested case D</>",
      "  </>",
      "  <gray *other>Fallback Case</>",
      "</>"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("input", "a"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Case A")
        .string("color", "red")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("input", "b"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Case B")
        .string("color", "green")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("input", "null")
        .withStatic("other_input", "C"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Nested case C")
        .string("color", "gold")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("input", "null")
        .withStatic("other_input", "D"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Nested case D")
        .string("color", "yellow")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("input", "null")
        .withStatic("other_input", "E"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("input", "asd"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Fallback Case")
        .string("color", "gray")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("input", null),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Fallback Case")
        .string("color", "gray")
    );
  }

  @Test
  public void shouldRenderInterpolationWithBinding() {
    TextWithAnchors text = new TextWithAnchors(
      "<red let-my_var=\"my_prefix & my_name & my_suffix\">Hello, {my_var}"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("my_prefix", "prefix ")
        .withStatic("my_name", "Steve")
        .withStatic("my_suffix", " suffix"),
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
    TextWithAnchors text = new TextWithAnchors(
      "<red",
      "  *for-char=\"my_chars\"",
      "  for-separator={ <aqua>separator }",
      "  for-reversed=" + reversed,
      "  let-index=\"loop.index\"",
      ">",
      "  {char} at index {index}"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("my_chars", Arrays.asList("A", "S", "T")),
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
    TextWithAnchors text = new TextWithAnchors(
      "<aqua><bold><hover-text value={<red>Hello, hover!}>Hover over me!"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
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
    TextWithAnchors text = new TextWithAnchors(
      "<container *for=\"1..3\" for-separator={<space/>} let-number=\"loop.index + 1\">{number}"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "1 2 3")
    );
  }

  @Test
  public void shouldGenerateAGradient() {
    TextWithAnchors text = new TextWithAnchors(
      "<gradient color=\"red\" color=\"blue\">Hello, <bold>world</>!"
    );

    makeColorizerCase(
      text,
      "Hello, world!",
      (index, letter) -> {
        if (index >= 7 && index <= 11)
          letter.bool("bold", true);
      },
      "#FF5555",
      "#F05563",
      "#E25571",
      "#D4557F",
      "#C6558D",
      "#B8559B",
      null,
      "#AA55AA",
      "#9B55B8",
      "#8D55C6",
      "#7F55D4",
      "#7155E2",
      "#6355F0"
    );
  }

  @Test
  public void shouldGenerateARainbow() {
    TextWithAnchors text = new TextWithAnchors(
      "<rainbow>I am the <b>coolest rainbow</b> on earth"
    );

    makeColorizerCase(
      text,
      "I am the coolest rainbow on earth",
      (index, letter) -> {
        if (index >= 9 && index <= 23)
          letter.bool("bold", true);
      },
      "#FF0000",
      null,
      "#FF3900",
      "#FF7100",
      null,
      "#FFAA00",
      "#FFE300",
      "#E3FF00",
      null,
      "#AAFF00",
      "#71FF00",
      "#39FF00",
      "#00FF00",
      "#00FF39",
      "#00FF71",
      "#00FFAA",
      null,
      "#00FFE3",
      "#00E3FF",
      "#00AAFF",
      "#0071FF",
      "#0039FF",
      "#0000FF",
      "#3900FF",
      null,
      "#7100FF",
      "#AA00FF",
      null,
      "#E300FF",
      "#FF00E3",
      "#FF00AA",
      "#FF0071",
      "#FF0039"
    );
  }

  @Test
  public void shouldDifferentiateBetweenIfAndUse() {
    TextWithAnchors text = new TextWithAnchors(
      "<red *if=\"a\" *use=\"b\">Hello, world!"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("a", true)
        .withStatic("b", true),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .string("color", "red")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("a", true)
        .withStatic("b", false),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("a", false)
        .withStatic("b", true),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("a", false)
        .withStatic("b", false),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
    );
  }

  @Test
  public void shouldSkipRainbowsOnUseIsFalse() {
    TextWithAnchors text = new TextWithAnchors(
      "<rainbow *use=\"false\">Hello, world!"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
    );
  }

  @Test
  public void shouldSkipStyleTagAttributesOnUseIsFalse() {
    TextWithAnchors text = new TextWithAnchors(
      "<style *use=\"a\" bold italic underlined color=\"red\" font=\"my.font\">Hello, world!"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("a", true),
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
      new EnvironmentBuilder()
        .withStatic("a", false),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
    );
  }

  @Test
  public void shouldNotRepeatInheritedStyle() {
    TextWithAnchors text = new TextWithAnchors(
      "<red><bold>hello</><italic>world <red>test</>"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
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
    TextWithAnchors text = new TextWithAnchors(
      "<red>",
      "  <bold>",
      "    I am bold and red!",
      "    <reset>",
      "      <red>I am just red!"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
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
    TextWithAnchors text = new TextWithAnchors(
      "<reset>Hello, world!"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.ITEM_LORE,
      new JsonObjectBuilder()
        .string("text", "Hello, world!")
        .bool("italic", false)
        .string("color", "white")
    );
  }

  @Test
  public void shouldAlwaysUseNearestValues() {
    TextWithAnchors text = new TextWithAnchors(
      // No color/font is terminated, so they nest
      "<red><style font=\"a\">first line<br/>",
      "<green><style font=\"b\">second line<br/>",
      "<blue><style font=\"c\">third line"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
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
      new TextWithAnchors(
        "<red><italic><green><italic>hello, world</green><blue><italic>test me out</blue>"
      ),
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
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
    TextWithAnchors text = new TextWithAnchors(
      "<red *for=\"1..5\">A</red>"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "AAAAA")
        .string("color", "red")
    );
  }

  @Test
  public void shouldJoinSubsequentTexts() {
    TextWithAnchors text = new TextWithAnchors(
      // Non-effective styled passages should also be interpreted as raw text
      "<red>Hello, <style [color]=\"null\">{a}</style> and {b}!"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("a", "first")
        .withStatic("b", "second"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, first and second!")
        .string("color", "red")
    );
  }

  @Test
  public void shouldJoinSubsequentTextsInATransition() {
    TextWithAnchors text = new TextWithAnchors(
      "<transition color=\"red\" color=\"blue\">Hello {a} world {b}!"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("a", "first")
        .withStatic("b", "second"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello first world second!")
        .string("color", "red")
    );
  }

  @Test
  public void shouldInterpolateMarkupValues() {
    TextWithAnchors text = new TextWithAnchors(
      "<red>before</> {markup_value} <blue>after</> {scalar_value}"
    );

    MarkupNode node = MarkupParser.parse("<bold><gold>I am a markup-value!", BuiltInTagRegistry.INSTANCE);

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("markup_value", node)
        .withStatic("scalar_value", "Hello, world!"),
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
    TextWithAnchors text = new TextWithAnchors(
      "<translate key=\"my.key\" [with]=\"first_node\"/>"
    );

    MarkupNode firstNode = MarkupParser.parse("<bold><gold>I am the first!", BuiltInTagRegistry.INSTANCE);

    JsonObjectBuilder firstNodeJson = new JsonObjectBuilder()
      .string("text", "I am the first!")
      .string("color", "gold")
      .bool("bold", true);

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("first_node", firstNode),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("translate", "my.key")
        .array("with", with -> (
          with.object(item -> firstNodeJson)
        ))
    );

    MarkupNode secondNode = MarkupParser.parse("<italic><red>I am the second!", BuiltInTagRegistry.INSTANCE);

    JsonObjectBuilder secondNodeJson = new JsonObjectBuilder()
      .string("text", "I am the second!")
      .string("color", "red")
      .bool("italic", true);

    text = new TextWithAnchors(
      "<translate key=\"my.key\" [with]=\"first_node\" [with]=\"second_node\"/>"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("first_node", firstNode)
        .withStatic("second_node", secondNode),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("translate", "my.key")
        .array("with", with -> (
          with
            .object(item -> firstNodeJson)
            .object(item -> secondNodeJson)
        ))
    );

    text = new TextWithAnchors(
      "<translate key=\"my.key\" [...with]=\"[first_node, second_node]\"/>"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("first_node", firstNode)
        .withStatic("second_node", secondNode),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("translate", "my.key")
        .array("with", with -> (
          with
            .object(item -> firstNodeJson)
            .object(item -> secondNodeJson)
        ))
    );

    text = new TextWithAnchors(
      "<translate key=\"my.key\" [with]=\"[first_node, second_node]\"/>"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("first_node", firstNode)
        .withStatic("second_node", secondNode),
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
    TextWithAnchors text = new TextWithAnchors(
      "<container",
      "  let-spacer={ <dark_gray><st>{' ' ** 15} }",
      "  let-line={ <red>Hello, world! }",
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
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
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
    TextWithAnchors text = new TextWithAnchors(
      "<red let-a=\"'a'\">{a ** 10}"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "aaaaaaaaaa")
        .string("color", "red")
    );
  }

  @Test
  public void shouldAllowBackwardsAccessOnLetBindings() {
    TextWithAnchors text = new TextWithAnchors(
      "<container",
      "  let-a=5",
      "  let-b=12",
      "  let-c=\"b - a\"",
      "  let-d=3",
      "  let-e=\"c ^ d\"",
      ">{e}"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "343")
    );
  }

  private void makeColorizerCase(
    TextWithAnchors input,
    String text,
    BiConsumer<Integer, JsonObjectBuilder> letterAndIndexConsumer,
    String... colors
  ) {
    makeCase(
      input,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> {
          char[] chars = text.toCharArray();

          for (int index = 0; index < chars.length; ++index) {
            int charIndex = index;
            char currentChar = chars[index];
            String currentColor = index < colors.length ? colors[index] : "<undefined>";

            extra.object(letter -> {
              letter.string("text", String.valueOf(currentChar));

              if (currentColor != null)
                letter.string("color", currentColor);

              if (letterAndIndexConsumer != null)
                letterAndIndexConsumer.accept(charIndex, letter);

              return letter;
            });
          }

          return extra;
        })
    );
  }

  private JsonElement sortKeysRecursively(JsonElement input) {
    if (input instanceof JsonArray) {
      JsonArray jsonArray = (JsonArray) input;

      for (int index = 0; index < jsonArray.size(); ++index) {
        jsonArray.set(index, sortKeysRecursively(jsonArray.get(index)));
      }

      return jsonArray;
    }

    if (input instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) input;
      JsonObject result = new JsonObject();

      List<String> jsonKeys = new ArrayList<>(jsonObject.keySet());
      jsonKeys.sort(String::compareTo);

      for (String key : jsonKeys) {
        JsonElement value = sortKeysRecursively(jsonObject.get(key));

        if (value instanceof JsonPrimitive && key.equals("color")) {
          AnsiStyleColor ansiColor = AnsiStyleColor.fromNameLowerOrNull(value.getAsString().toLowerCase());

          if (ansiColor != null)
            value = new JsonPrimitive(PackedColor.asNonAlphaHex(ansiColor.packedColor));
        }

        result.add(key, value);
      }

      return result;
    }

    if (input instanceof JsonPrimitive || input instanceof JsonNull)
      return input;

    throw new IllegalStateException("Unaccounted-for json-element: " + input.getClass());
  }

  private void makeCase(
    TextWithAnchors input,
    InterpretationEnvironment baseEnvironment,
    SlotType slot,
    JsonBuilder expectedResult
  ) {
    MarkupNode actualNode;

    try {
      actualNode = MarkupParser.parse(input.text, BuiltInTagRegistry.INSTANCE);
    } catch (MarkupParseException e) {
      System.out.println(String.join("\n", e.makeErrorScreen()));
      Assertions.fail("Threw an error:", e);
      return;
    }

    JsonElement expectedJson;

    if (expectedResult instanceof JsonObjectBuilder) {
      JsonArray array = new JsonArray();
      array.add(expectedResult.build());
      expectedJson = array;
    }
    else if (expectedResult instanceof JsonArrayBuilder)
      expectedJson = expectedResult.build();
    else
      throw new IllegalStateException("Unknown json-builder: " + expectedResult.getClass());

    List<Object> resultItems = MarkupInterpreter.interpret(
      componentConstructor,
      baseEnvironment,
      slot, actualNode
    );

    JsonArray actualJson = new JsonArray();

    for (Object resultItem : resultItems)
      actualJson.add((JsonElement) resultItem);

    Assertions.assertEquals(
      gsonInstance.toJson(sortKeysRecursively(expectedJson)),
      gsonInstance.toJson(sortKeysRecursively(actualJson))
    );
  }
}
