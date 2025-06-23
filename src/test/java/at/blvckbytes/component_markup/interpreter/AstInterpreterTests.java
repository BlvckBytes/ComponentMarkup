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
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class AstInterpreterTests {

  private static final IExpressionEvaluator expressionEvaluator = new GPEEE(Logger.getAnonymousLogger());
  private static final Gson gsonInstance = new GsonBuilder().setPrettyPrinting().create();
  private static final ComponentConstructor componentConstructor = new JsonComponentConstructor();
  private static final Logger logger = Logger.getAnonymousLogger();

  @Test
  public void shouldInterpretSimpleText() {
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
