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

  private static final Logger logger = Logger.getAnonymousLogger();
  private static final ExpressionInterpreter interpreter = new ExpressionInterpreter(logger);

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
  public void shouldTransformReverse() {
    makeCase(
      "'before ' & ~<my_string & ' test'",
      new EnvironmentBuilder()
        .withStatic("my_string", "Hello, World"),
      "before dlroW ,olleH test"
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

  @Test
  public void shouldSubstringInAllPermutations() {
    InterpretationEnvironment environment = new EnvironmentBuilder()
      .withStatic("input", "ABCDEFGHIJ");

    // Full string
    makeCase("input[:]", environment, "ABCDEFGHIJ");

    // Start only
    makeCase("input[0:]", environment, "ABCDEFGHIJ");
    makeCase("input[3:]", environment, "DEFGHIJ");
    makeCase("input[9:]", environment, "J");
    makeCase("input[10:]", environment, "");
    makeCase("input[-1:]", environment, "J");
    makeCase("input[-3:]", environment, "HIJ");

    // End only
    makeCase("input[:0]", environment, "A");
    makeCase("input[:3]", environment, "ABCD");
    makeCase("input[:9]", environment, "ABCDEFGHIJ");
    makeCase("input[:10]", environment, "ABCDEFGHIJ");
    makeCase("input[:-1]", environment, "ABCDEFGHIJ");
    makeCase("input[:-3]", environment, "ABCDEFGH");

    // Both bounds positive
    makeCase("input[2:5]", environment, "CDEF");
    makeCase("input[5:2]", environment, "");

    // Both bounds negative
    makeCase("input[-4:-2]", environment, "GHI");
    makeCase("input[-1:-1]", environment, "J");
    makeCase("input[-10:-1]", environment, "ABCDEFGHIJ");
    makeCase("input[-100:-1]", environment, "ABCDEFGHIJ");

    // Mixed signs
    makeCase("input[-3:9]", environment, "HIJ");
    makeCase("input[3:-2]", environment, "DEFGHI");
    makeCase("input[8:-2]", environment, "I");
    makeCase("input[-2:5]", environment, "");

    // Out-of-bounds (positive)
    makeCase("input[0:100]", environment, "ABCDEFGHIJ");
    makeCase("input[10:12]", environment, "");
    makeCase("input[5:100]", environment, "FGHIJ");

    // Out-of-bounds (negative)
    makeCase("input[-100:-90]", environment, "");
    makeCase("input[-100:2]", environment, "ABC");
    makeCase("input[-2:100]", environment, "IJ");

    // Single Character
    makeCase("input[0:0]", environment, "A");
    makeCase("input[9:9]", environment, "J");

    // Empty input
    makeCase("''[:]", environment, "");
    makeCase("''[0:]", environment, "");
    makeCase("''[:0]", environment, "");
    makeCase("''[-1:]", environment, "");
    makeCase("''[:-1]", environment, "");
    makeCase("''[-1:-1]", environment, "");

    // Single char literals
    makeCase("input['C':]", environment, "CDEFGHIJ");
    makeCase("input[:'C']", environment, "ABC");
    makeCase("input[3:'I']", environment, "DEFGHI");
    makeCase("input['B':5]", environment, "BCDEF");
    makeCase("input['B':-2]", environment, "BCDEFGHI");
    makeCase("input[-4:'H']", environment, "GH");

    // Multi char literals
    makeCase("input['ABC':'GH']", environment, "ABCDEFG");

    // Non-contained literals
    makeCase("input['Z':'X']", environment, "ABCDEFGHIJ");
  }

  @Test
  public void shouldRepeatAString() {
    makeCase("'hello' ** 5", InterpretationEnvironment.EMPTY_ENVIRONMENT, "hellohellohellohellohello");
  }

  private void makeCase(String expression, InterpretationEnvironment environment, Object expectedResult) {
    ExpressionNode node = ExpressionParser.parse(expression);
    Assertions.assertEquals(expectedResult, interpreter.interpret(node, environment));
  }
}
