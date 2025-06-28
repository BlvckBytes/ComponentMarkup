package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ExpressionInterpreterTests {

  private static final ExpressionInterpreter interpreter = new ExpressionInterpreter(Logger.getAnonymousLogger());

  @Test
  public void shouldTransformUpperCase() {
    makeCase(
      "'before ' & ~^my_string & ' after'",
      new EnvironmentBuilder()
        .withStatic("my_string", "helLo, woRld"),
      "before HELLO, WORLD after"
    );
  }

  @Test
  public void shouldTransformLowerCase() {
    makeCase(
      "'before ' & ~_my_string & ' after'",
      new EnvironmentBuilder()
        .withStatic("my_string", "helLo, woRld"),
      "before hello, world after"
    );
  }

  @Test
  public void shouldTransformTitleCase() {
    makeCase(
      "'before ' & ~#my_string & ' test'",
      new EnvironmentBuilder()
        .withStatic("my_string", "helLo, woRld"),
      "before Hello, World test"
    );
  }

  @Test
  public void shouldTransformSlugify() {
    makeCase(
      "'before ' & ~-my_string & ' test'",
      new EnvironmentBuilder()
        .withStatic("my_string", "helLö, @wöRld"),
      "before hellö-wörld test"
    );
  }

  @Test
  public void shouldTransformAsciify() {
    makeCase(
      "'before ' & ~?my_string & ' test'",
      new EnvironmentBuilder()
        .withStatic("my_string", "helLö, wèRld"),
      "before helLo, weRld test"
    );
  }

  @Test
  public void shouldTransformTrim() {
    makeCase(
      "'before ' & ~|my_string & ' test'",
      new EnvironmentBuilder()
        .withStatic("my_string", "  Hello, World  "),
      "before Hello, World test"
    );
  }

  @Test
  public void shouldExplodeStringWithNullOrEmptyString() {
    List<String> result = Arrays.asList("h", "e", "l", "l", "o");

    makeCase(
      "'hello' @ null",
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      result
    );

    makeCase(
      "'hello' @ ''",
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      result
    );
  }

  @Test
  public void shouldExplodeStringWithDelimiter() {
    makeCase(
      "'first second third' @ ' '",
      InterpretationEnvironment.EMPTY_ENVIRONMENT,
      Arrays.asList("first", "second", "third")
    );
  }

  @Test
  public void shouldExplodeStringWithRegex() {
    InterpretationEnvironment environment = new EnvironmentBuilder()
      .withStatic("input", "first0second1third2fourth");

    makeCase(
      "input @ '[0-9]'",
      environment,
      Collections.singletonList(environment.getVariableValue("input"))
    );

    makeCase(
      "input @@ '[0-9]'",
      environment,
      Arrays.asList("first", "second", "third", "fourth")
    );
  }

  private void makeCase(String expression, InterpretationEnvironment environment, Object expectedResult) {
    ExpressionNode node = ExpressionParser.parse(expression);
    Assertions.assertEquals(expectedResult, interpreter.interpret(node, environment));
  }
}
