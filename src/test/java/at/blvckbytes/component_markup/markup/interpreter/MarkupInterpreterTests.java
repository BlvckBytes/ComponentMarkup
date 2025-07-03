package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.TagRegistry;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.expression.interpreter.EnvironmentBuilder;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import com.google.gson.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class MarkupInterpreterTests {

  private static final Logger logger = Logger.getAnonymousLogger();
  private static final TagRegistry builtInTagRegistry = new BuiltInTagRegistry(logger);
  private static final ExpressionInterpreter expressionInterpreter = new ExpressionInterpreter(logger);
  private static final Gson gsonInstance = new GsonBuilder().setPrettyPrinting().create();
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
  public void shouldRenderInterpolationWithBinding() {
    TextWithAnchors text = new TextWithAnchors(
      "<red let-my_var=\"my_prefix & my_name & my_suffix\">Hello, {{my_var}}"
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("my_prefix", "prefix ")
        .withStatic("my_name", "Steve")
        .withStatic("my_suffix", " suffix"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .string("color", "red")
        .array("extra", extra -> (
          extra
            .object(interpolation -> (
              interpolation
                .string("text", "Hello, ")
            ))
            .object(interpolation -> (
              interpolation
                .string("text", "prefix Steve suffix")
            ))
        ))
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
      "  {{char}} at index {{index}}"
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
                .string("text", "")
                .string("color", "red")
                .array("extra", containerExtra -> (
                  containerExtra
                    .object(interpolation -> (
                      interpolation
                        .string("text", "A")
                    ))
                    .object(interpolation -> (
                      interpolation
                        .string("text", " at index ")
                    ))
                    .object(interpolation -> (
                      interpolation
                        .string("text", "0")
                    ))
                ))
            ))
            .object(interpolation -> (
              interpolation
                .string("text", "separator")
                .string("color", "aqua")
            ))
            .object(container -> (
              container
                .string("text", "")
                .string("color", "red")
                .array("extra", containerExtra -> (
                  containerExtra
                    .object(interpolation -> (
                      interpolation
                        .string("text", "S")
                    ))
                    .object(interpolation -> (
                      interpolation
                        .string("text", " at index ")
                    ))
                    .object(interpolation -> (
                      interpolation
                        .string("text", "1")
                    ))
                ))
            ))
            .object(interpolation -> (
              interpolation
                .string("text", "separator")
                .string("color", "aqua")
            ))
            .object(container -> (
              container
                .string("text", "")
                .string("color", "red")
                .array("extra", containerExtra -> (
                  containerExtra
                  .object(interpolation -> (
                    interpolation
                      .string("text", "T")
                  ))
                  .object(interpolation -> (
                    interpolation
                      .string("text", " at index ")
                  ))
                  .object(interpolation -> (
                    interpolation
                      .string("text", "2")
                  ))
                ))
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
      "<red *for=\"1..3\" let-number=\"loop.index + 1\">{{number}}"
    );

    makeCase(
      text,
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "1")
                .string("color", "red")
            ))
            .object(item -> (
              item
                .string("text", "2")
                .string("color", "red")
            ))
            .object(item -> (
              item
                .string("text", "3")
                .string("color", "red")
            ))
        ))
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
                .string("text", "")
                // TODO: These should be joined, of course, but that's another TODO on my list
                .array("extra", innerExtra -> (
                  innerExtra
                    .object(innerItem -> (
                      innerItem
                        .string("text", "world ")
                    ))
                    .object(innerItem -> (
                      innerItem
                        .string("text", "test")
                    ))
                ))
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

    if (input instanceof JsonPrimitive)
      return input;

    throw new IllegalStateException("Unaccounted-for json-element: " + input.getClass());
  }

  private void makeCase(
    TextWithAnchors input,
    InterpretationEnvironment baseEnvironment,
    SlotType slot,
    JsonBuilder expectedResult
  ) {
    MarkupNode actualNode = MarkupParser.parse(input.text, builtInTagRegistry, logger);

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
      componentConstructor, expressionInterpreter,
      baseEnvironment,
      logger, SlotContext.getForSlot(slot), actualNode
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
