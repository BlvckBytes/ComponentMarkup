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
      new JsonObjectBuilder()
        .string("text", "My flag is true!")
        .string("color", "red")
        .bool("italic", true)
    );

    makeCase(
      text,
      new EnvironmentBuilder()
        .withStatic("my_flag", false),
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

      for (String key : jsonKeys)
        result.add(key, sortKeysRecursively(jsonObject.get(key)));

      return result;
    }

    if (input instanceof JsonPrimitive)
      return input;

    throw new IllegalStateException("Unaccounted-for json-element: " + input.getClass());
  }

  private void makeCase(TextWithAnchors input, InterpretationEnvironment baseEnvironment, JsonBuilder expectedResult) {
    MarkupNode actualNode = MarkupParser.parse(input.text, builtInTagRegistry, logger);

    char breakChar;
    JsonElement expectedJson;

    if (expectedResult instanceof JsonObjectBuilder) {
      JsonArray array = new JsonArray();
      array.add(expectedResult.build());
      expectedJson = array;
      breakChar = '\n';
    }
    else if (expectedResult instanceof JsonArrayBuilder) {
      expectedJson = expectedResult.build();
      breakChar = 0;
    }
    else
      throw new IllegalStateException("Unknown json-builder: " + expectedResult.getClass());

    List<Object> resultItems = MarkupInterpreter.interpret(
      componentConstructor, expressionInterpreter,
      baseEnvironment,
      logger, breakChar, actualNode
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
