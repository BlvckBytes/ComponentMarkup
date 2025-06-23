package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.parser.AstParser;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import at.blvckbytes.component_markup.xml.XmlEventParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.Logger;

public class AstInterpreterTests {

  private static final IExpressionEvaluator expressionEvaluator = new GPEEE(Logger.getAnonymousLogger());
  private static final Gson gsonInstance = new GsonBuilder().setPrettyPrinting().create();
  private static final ComponentConstructor componentConstructor = new JsonComponentConstructor();
  private static final Logger logger = Logger.getAnonymousLogger();

  @Test
  public void shouldRenderSimpleText() {
    TextWithAnchors text = new TextWithAnchors(
      "<red><bold>Hello, world! :)"
    );

    makeCase(
      text,
      GPEEE.EMPTY_ENVIRONMENT,
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
      new EvaluationEnvironmentBuilder()
        .withVariable("my_flag", true)
        .build(),
      new JsonObjectBuilder()
        .string("text", "My flag is true!")
        .string("color", "red")
        .bool("italic", true)
    );

    makeCase(
      text,
      new EvaluationEnvironmentBuilder()
        .withVariable("my_flag", false)
        .build(),
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
      new EvaluationEnvironmentBuilder()
        .withVariable("my_prefix", "prefix ")
        .withVariable("my_name", "Steve")
        .withVariable("my_suffix", " suffix")
        .build(),
      new JsonObjectBuilder()
        .string("text", "")
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
        .string("color", "red")
    );
  }

  @Test
  public void shouldRenderForLoop() {
    TextWithAnchors text = new TextWithAnchors(
      "<red",
      "  *for-char=\"my_chars\"",
      "  for-separator={ <aqua>separator }",
      "  let-index=\"loop.index\"",
      ">",
      "  {{char}} at index {{index}}"
    );

    makeCase(
      text,
      new EvaluationEnvironmentBuilder()
        .withVariable("my_chars", Arrays.asList("A", "S", "T"))
        .build(),
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(container -> (
              container
                .string("text", "")
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
                .string("color", "red")
            ))
            .object(interpolation -> (
              interpolation
                .string("text", "separator")
                .string("color", "aqua")
            ))
            .object(container -> (
              container
                .string("text", "")
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
                .string("color", "red")
            ))
            .object(interpolation -> (
              interpolation
                .string("text", "separator")
                .string("color", "aqua")
            ))
            .object(container -> (
              container
                .string("text", "")
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
                .string("color", "red")
            ))
        ))
    );
  }

  private void makeCase(TextWithAnchors input, IEvaluationEnvironment baseEnvironment, JsonBuilder expectedResult) {
    AstParser parser = new AstParser(BuiltInTagRegistry.get(), expressionEvaluator);
    XmlEventParser.parse(input.text, parser);
    AstNode actualAst = parser.getResult();

    String expectedJson = gsonInstance.toJson(expectedResult.build());
    String actualJson;

    if (expectedResult instanceof JsonObjectBuilder) {
      actualJson = gsonInstance.toJson(
        AstInterpreter.interpretSingle(componentConstructor, expressionEvaluator, baseEnvironment, logger, '\n', actualAst)
      );
    }

    else if (expectedResult instanceof JsonArrayBuilder) {
      actualJson = gsonInstance.toJson(
        AstInterpreter.interpretMulti(componentConstructor, expressionEvaluator, baseEnvironment, logger, actualAst)
      );
    }

    else
      throw new IllegalStateException("Unknown json-builder: " + expectedResult.getClass());

    Assertions.assertEquals(expectedJson, actualJson);
  }
}
