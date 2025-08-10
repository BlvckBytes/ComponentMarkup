/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.util.StringView;
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
  public void shouldExplodeStringWithNullOrEmptyString() {
    List<String> result = Arrays.asList("h", "e", "l", "l", "o");

    makeCase(
      "'hello' @ null",
      new InterpretationEnvironment(),
      result
    );

    makeCase(
      "'hello' @ ''",
      new InterpretationEnvironment(),
      result
    );
  }

  @Test
  public void shouldExplodeStringWithDelimiter() {
    makeCase(
      "'first second third' @ ' '",
      new InterpretationEnvironment(),
      Arrays.asList("first", "second", "third")
    );
  }

  @Test
  public void shouldExplodeStringWithRegex() {
    InterpretationEnvironment environment = new InterpretationEnvironment()
      .withVariable("input", "first0second1third2fourth");

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
    makeCase("'abc' :: 'abcde'", false);
    makeCase("'abcde' :: 'abc'", true);
    makeCase("'ABCDE' :: 'abc'", false);
    makeCase("lower('ABCDE') :: 'abc'", true);
    makeCase("null :: 'abc'", false);
    makeCase("'abc' :: null", false);
    makeCase("null :: null", true);
    makeCase("'' :: 'abc'", false);
    makeCase("'a' :: ''", true);
    makeCase("'' :: ''", true);
    makeCase("'abc' :: 123", false);
    makeCase("123 :: '123'", true);
  }

  @Test
  public void shouldCheckIfRegexMatches() {
    makeCase("'ABCDEF' ::: '[A-Z]{4,}'", true);
    makeCase("'ABC' ::: '[A-Z]{4,}'", false);
    makeCase("'foobar123' ::: '\\d+'", true);
    makeCase("'foobar' ::: '\\d+'", false);
    makeCase("'hello world' ::: 'world'", true);
    makeCase("'start middle end' ::: '^start'", true);
    makeCase("'start middle end' ::: 'end$'", true);
    makeCase("'start middle end' ::: '^middle'", false);
    makeCase("'start middle end' ::: 'middle$'", false);
    makeCase("'Hello' ::: 'hello'", false);
    makeCase("lower('Hello') ::: 'hello'", true);
    makeCase("'Hello' ::: '(?i)hello'", true);
    makeCase("'cat' ::: 'cat|dog'", true);
    makeCase("'dog' ::: 'cat|dog'", true);
    makeCase("'cow' ::: 'cat|dog'", false);
    makeCase("'foobar' ::: '(foo)(bar)'", true);
    makeCase("'aaaab' ::: 'a{3,5}b'", true);
    makeCase("'aaab' ::: 'a{4,5}b'", false);
    makeCase("'price: $12.50' ::: '\\$\\d+\\.\\d{2}'", true);
    makeCase("'just text' ::: '\\$\\d+'", false);
    makeCase("12345 ::: '\\d+'", true);
  }

  @Test
  public void shouldHandleFullBranchingOperator() {
    String expression = "a ? \"first\" : \"second\"";

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
    String expression = "a ? \"first\"";

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

  private void makeCase(String expression, Object expectedResult) {
    makeCase(expression, new InterpretationEnvironment(), expectedResult);
  }

  private void makeCase(String expression, InterpretationEnvironment environment, Object expectedResult) {
    ExpressionNode node = ExpressionParser.parse(StringView.of(expression), null);
    Assertions.assertEquals(Jsonifier.jsonify(expectedResult), Jsonifier.jsonify(ExpressionInterpreter.interpret(node, environment)));
  }
}
