/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExpressionInterpreterTests {

  @Test
  public void shouldTransformUpperCase() {
    makeCase(
      "'before ' & upper(my_string) & ' after'",
      new InterpretationEnvironment()
        .withVariable("my_string", "helLo, woRld"),
      "before HELLO, WORLD after"
    );
  }

  @Test
  public void shouldTransformLowerCase() {
    makeCase(
      "'before ' & lower(my_string) & ' after'",
      new InterpretationEnvironment()
        .withVariable("my_string", "helLo, woRld"),
      "before hello, world after"
    );
  }

  @Test
  public void shouldTransformTitleCase() {
    makeCase(
      "'before ' & title(my_string) & ' test'",
      new InterpretationEnvironment()
        .withVariable("my_string", "helLo, woRld"),
      "before Hello, World test"
    );
  }

  @Test
  public void shouldTransformSlugify() {
    makeCase(
      "'before ' & slugify(my_string) & ' test'",
      new InterpretationEnvironment()
        .withVariable("my_string", "helLö, @wöRld"),
      "before hellö-wörld test"
    );
  }

  @Test
  public void shouldTransformAsciify() {
    makeCase(
      "'before ' & asciify(my_string) & ' test'",
      new InterpretationEnvironment()
        .withVariable("my_string", "helLö, wèRld"),
      "before helLo, weRld test"
    );
  }

  @Test
  public void shouldTransformTrim() {
    makeCase(
      "'before ' & trim(my_string) & ' test'",
      new InterpretationEnvironment()
        .withVariable("my_string", "  Hello, World  "),
      "before Hello, World test"
    );
  }

  @Test
  public void shouldTransformReverse() {
    makeCase(
      "'before ' & reverse(my_string) & ' test'",
      new InterpretationEnvironment()
        .withVariable("my_string", "Hello, World"),
      "before dlroW ,olleH test"
    );
  }

  @Test
  public void shouldSplitStringWithNullOrEmptyString() {
    List<String> result = Arrays.asList("h", "e", "l", "l", "o");

    makeCase(
      "'hello' split null",
      new InterpretationEnvironment(),
      result
    );

    makeCase(
      "'hello' split ''",
      new InterpretationEnvironment(),
      result
    );
  }

  @Test
  public void shouldSplitStringWithDelimiter() {
    makeCase(
      "'first second third' split ' '",
      new InterpretationEnvironment(),
      Arrays.asList("first", "second", "third")
    );
  }

  @Test
  public void shouldSplitStringWithRegex() {
    InterpretationEnvironment environment = new InterpretationEnvironment()
      .withVariable("input", "first0second1third2fourth");

    makeCase(
      "input split '[0-9]'",
      environment,
      Collections.singletonList(environment.getVariableValue("input"))
    );

    makeCase(
      "input rsplit '[0-9]'",
      environment,
      Arrays.asList("first", "second", "third", "fourth")
    );
  }

  @Test
  public void shouldSubstringInAllPermutations() {
    InterpretationEnvironment environment = new InterpretationEnvironment()
      .withVariable("input", "ABCDEFGHIJ");

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
    makeCase("'hello' ** 5", new InterpretationEnvironment(), "hellohellohellohellohello");
  }

  @Test
  public void shouldCheckIfStringContains() {
    makeCase("'abcde' in 'abc'", false);
    makeCase("'abc' in 'abcde'", true);
    makeCase("'abc' in 'ABCDE'", false);
    makeCase("'abc' in lower('ABCDE')", true);
    makeCase("'abc' in null", false);
    makeCase("null in 'abc'", false);
    makeCase("null in null", true);
    makeCase("'abc' in ''", false);
    makeCase("'' in 'a'", true);
    makeCase("'' in ''", true);
    makeCase("123 in 'abc'", false);
    makeCase("'123' in 123", true);
  }

  @Test
  public void shouldCheckIfRegexMatches() {
    makeCase("'ABCDEF' matches '[A-Z]{4,}'", true);
    makeCase("'ABC' matches '[A-Z]{4,}'", false);
    makeCase("'foobar123' matches '\\d+'", true);
    makeCase("'foobar' matches '\\d+'", false);
    makeCase("'hello world' matches 'world'", true);
    makeCase("'start middle end' matches '^start'", true);
    makeCase("'start middle end' matches 'end$'", true);
    makeCase("'start middle end' matches '^middle'", false);
    makeCase("'start middle end' matches 'middle$'", false);
    makeCase("'Hello' matches 'hello'", false);
    makeCase("lower('Hello') matches 'hello'", true);
    makeCase("'Hello' matches '(?i)hello'", true);
    makeCase("'cat' matches 'cat|dog'", true);
    makeCase("'dog' matches 'cat|dog'", true);
    makeCase("'cow' matches 'cat|dog'", false);
    makeCase("'foobar' matches '(foo)(bar)'", true);
    makeCase("'aaaab' matches 'a{3,5}b'", true);
    makeCase("'aaab' matches 'a{4,5}b'", false);
    makeCase("'price: $12.50' matches '\\$\\d+\\.\\d{2}'", true);
    makeCase("'just text' matches '\\$\\d+'", false);
    makeCase("12345 matches '\\d+'", true);
  }

  @Test
  public void shouldHandleFullBranchingOperator() {
    String expression = "a then \"first\" else \"second\"";

    makeCase(
      expression,
      new InterpretationEnvironment()
        .withVariable("a", true),
      "first"
    );

    makeCase(
      expression,
      new InterpretationEnvironment()
        .withVariable("a", false),
      "second"
    );
  }

  @Test
  public void shouldHandleHalfBranchingOperator() {
    String expression = "a then \"first\"";

    makeCase(
      expression,
      new InterpretationEnvironment()
        .withVariable("a", true),
      "first"
    );

    makeCase(
      expression,
      new InterpretationEnvironment()
        .withVariable("a", false),
      null
    );
  }

  @Test
  public void shouldHandleSimpleInterpolations() {
    makeCase(
      "`hello {a} world \\` {b} :)!`",
      new InterpretationEnvironment()
        .withVariable("a", 5)
        .withVariable("b", 6),
      "hello 5 world ` 6 :)!"
    );
  }

  @Test
  public void shouldHandleComplexInterpolation() {
    makeCase(
      "`hello {`pre {c} {d} post`} world \\` {b} :)!`",
      new InterpretationEnvironment()
        .withVariable("a", 5)
        .withVariable("b", 6)
        .withVariable("c", 7)
        .withVariable("d", 8),
      "hello pre 7 8 post world ` 6 :)!"
    );
  }

  @Test
  public void shouldGenerateRanges() {
    makeCase(
      "1..1",
      new InterpretationEnvironment(),
      Collections.singletonList(1)
    );

    makeCase(
      "1..5",
      new InterpretationEnvironment(),
      Arrays.asList(1, 2, 3, 4, 5)
    );

    makeCase(
      "4..a",
      new InterpretationEnvironment()
        .withVariable("a", 10),
      Arrays.asList(4, 5, 6, 7, 8, 9, 10)
    );

    makeCase(
      "a..10",
      new InterpretationEnvironment()
        .withVariable("a", 4),
      Arrays.asList(4, 5, 6, 7, 8, 9, 10)
    );

    makeCase(
      "a..b",
      new InterpretationEnvironment()
        .withVariable("a", 4)
        .withVariable("b", 10),
      Arrays.asList(4, 5, 6, 7, 8, 9, 10)
    );
  }

  @Test
  public void shouldCastToLong() {
    makeCase("5 + long(3.8)", null, 8);
  }

  @Test
  public void shouldCastToDouble() {
    makeCase("5 / double(2)", null, 2.5);
  }

  @Test
  public void shouldFloorCeilAndRoundDouble() {
    makeCase("floor(2.6)", null, 2.0);
    makeCase("ceil(2.3)", null, 3.0);
    makeCase("round(2.4)", null, 2.0);
    makeCase("round(2.5)", null, 3.0);
    makeCase("round(2.6)", null, 3.0);
  }

  @Test
  public void shouldPassThroughSingularMinOrMaxValue() {
    makeCase("max(5)", null, 5);
    makeCase("max(-.2)", null, -.2);
    makeCase("min(5)", null, 5);
    makeCase("min(-.2)", null, -.2);
  }

  @Test
  public void shouldMinOrMaxVariadicValues() {
    makeCase("max(4, -2, 5, 3, -1)", null, 5);
    makeCase("min(4, -2, 5, 3, -1)", null, -2);
  }

  private void makeCase(String expression, Object expectedResult) {
    makeCase(expression, new InterpretationEnvironment(), expectedResult);
  }

  private void makeCase(String expression, @Nullable InterpretationEnvironment environment, Object expectedResult) {
    if (environment == null)
      environment = new InterpretationEnvironment();

    ExpressionNode node = ExpressionParser.parse(StringView.of(expression), null);
    Assertions.assertEquals(Jsonifier.jsonify(expectedResult), Jsonifier.jsonify(ExpressionInterpreter.interpret(node, environment)));
  }
}
