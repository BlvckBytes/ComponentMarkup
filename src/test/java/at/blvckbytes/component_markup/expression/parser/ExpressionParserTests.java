package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizerTests;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.xml.TextWithAnchors;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ExpressionParserTests {

  // TODO: (Decide and) test where arrays are allowed to be specified
  // TODO: Write cases for all error-types and indices

  @Test
  public void shouldParseEmptyInputAsNull() {
    TextWithAnchors text = new TextWithAnchors(
      ""
    );

    makeCase(
      text,
      null
    );
  }

  @Test
  public void shouldParseEqualPrecedences() {
    TextWithAnchors text = new TextWithAnchors(
      // (((a + 5) - c) + d)
      "@a @+ @5 @- @c @+ @d"
    );

    makeCase(
      text,
      infix(
        infix(
          infix(
            terminal("a", text.anchorIndex(0)),
            InfixOperator.ADDITION,
            text.anchorIndex(1),
            terminal(5, text.anchorIndex(2))
          ),
          InfixOperator.SUBTRACTION,
          text.anchorIndex(3),
          terminal("c", text.anchorIndex(4))
        ),
        InfixOperator.ADDITION,
        text.anchorIndex(5),
        terminal("d", text.anchorIndex(6))
      )
    );
  }

  @Test
  public void shouldParseClimbingPrecedences() {
    TextWithAnchors text = new TextWithAnchors(
      // (a + ((b * 3) * d))
      "@a @+ @b @* @3 @* @d"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.anchorIndex(0)),
        InfixOperator.ADDITION,
        text.anchorIndex(1),
        infix(
          infix(
            terminal("b", text.anchorIndex(2)),
            InfixOperator.MULTIPLICATION,
            text.anchorIndex(3),
            terminal(3, text.anchorIndex(4))
          ),
          InfixOperator.MULTIPLICATION,
          text.anchorIndex(5),
          terminal("d", text.anchorIndex(6))
        )
      )
    );
  }

  @Test
  public void shouldParseFallingPrecedences() {
    TextWithAnchors text = new TextWithAnchors(
      // (((a ^ 2) * c) + d)
      "@a @^ @2 @* @c @+ @d"
    );

    makeCase(
      text,
      infix(
        infix(
          infix(
            terminal("a", text.anchorIndex(0)),
            InfixOperator.EXPONENTIATION,
            text.anchorIndex(1),
            terminal(2, text.anchorIndex(2))
          ),
          InfixOperator.MULTIPLICATION,
          text.anchorIndex(3),
          terminal("c", text.anchorIndex(4))
        ),
        InfixOperator.ADDITION,
        text.anchorIndex(5),
        terminal("d", text.anchorIndex(6))
      )
    );
  }

  @Test
  public void shouldParseAlternatingPrecedences() {
    TextWithAnchors text = new TextWithAnchors(
      // ((a + (5 * c)) - ((d / 2) % x))
      "@a @+ @5 @* @c @- @d @/ @2 @% @x"
    );

    makeCase(
      text,
      infix(
        infix(
          terminal("a", text.anchorIndex(0)),
          InfixOperator.ADDITION,
          text.anchorIndex(1),
          infix(
            terminal(5, text.anchorIndex(2)),
            InfixOperator.MULTIPLICATION,
            text.anchorIndex(3),
            terminal("c", text.anchorIndex(4))
          )
        ),
        InfixOperator.SUBTRACTION,
        text.anchorIndex(5),
        infix(
          infix(
            terminal("d", text.anchorIndex(6)),
            InfixOperator.DIVISION,
            text.anchorIndex(7),
            terminal(2, text.anchorIndex(8))
          ),
          InfixOperator.MODULO,
          text.anchorIndex(9),
          terminal("x", text.anchorIndex(10))
        )
      )
    );
  }

  @Test
  public void shouldParseExponentiationWithRightAssociativity() {
    TextWithAnchors text = new TextWithAnchors(
      // (a ^ (b ^ (c ^ d)))
      "@a @^ @b @^ @c @^ @d"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.anchorIndex(0)),
        InfixOperator.EXPONENTIATION,
        text.anchorIndex(1),
        infix(
          terminal("b", text.anchorIndex(2)),
          InfixOperator.EXPONENTIATION,
          text.anchorIndex(3),
          infix(
            terminal("c", text.anchorIndex(4)),
            InfixOperator.EXPONENTIATION,
            text.anchorIndex(5),
            terminal("d", text.anchorIndex(6))
          )
        )
      )
    );
  }

  @Test
  public void shouldRespectParentheses() {
    TextWithAnchors text = new TextWithAnchors(
      // ((a + b) * (c - d))
      "(@a @+ @b) @* (@c @- @d)"
    );

    makeCase(
      text,
      infix(
        infix(
          terminal("a", text.anchorIndex(0)),
          InfixOperator.ADDITION,
          text.anchorIndex(1),
          terminal("b", text.anchorIndex(2))
        ),
        InfixOperator.MULTIPLICATION,
        text.anchorIndex(3),
        infix(
          terminal("c", text.anchorIndex(4)),
          InfixOperator.SUBTRACTION,
          text.anchorIndex(5),
          terminal("d", text.anchorIndex(6))
        )
      )
    );
  }

  @Test
  public void shouldParsePrefixOperations() {
    TextWithAnchors text = new TextWithAnchors(
      "@25 @* @-@3"
    );

    makeCase(
      text,
      infix(
        terminal(25, text.anchorIndex(0)),
        InfixOperator.MULTIPLICATION,
        text.anchorIndex(1),
        prefix(
          terminal(3, text.anchorIndex(3)),
          PrefixOperator.FLIP_SIGN,
          text.anchorIndex(2)
        )
      )
    );

    text = new TextWithAnchors(
      "@-@3"
    );

    makeCase(
      text,
      prefix(
        terminal(3, text.anchorIndex(1)),
        PrefixOperator.FLIP_SIGN,
        text.anchorIndex(0)
      )
    );

    text = new TextWithAnchors(
      "@a @&& @!@b"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.anchorIndex(0)),
        InfixOperator.CONJUNCTION,
        text.anchorIndex(1),
        prefix(
          terminal("b", text.anchorIndex(3)),
          PrefixOperator.NEGATION,
          text.anchorIndex(2)
        )
      )
    );

    text = new TextWithAnchors(
      "@!@b"
    );

    makeCase(
      text,
      prefix(
        terminal("b", text.anchorIndex(1)),
        PrefixOperator.NEGATION,
        text.anchorIndex(0)
      )
    );
  }

  @Test
  public void shouldParseSubstringOperator() {
    makeSubstringCase(null, null);
    makeSubstringCase(25, null);
    makeSubstringCase(null, 25);
    makeSubstringCase(20, 25);
  }

  private void makeSubstringCase(@Nullable Object lowerBound, @Nullable Object upperBound) {
    String lowerExpression = lowerBound == null ? "" : String.valueOf(lowerBound);
    String upperExpression = upperBound == null ? "" : String.valueOf(upperBound);

    TextWithAnchors text = new TextWithAnchors(
      "@a@[@" + lowerExpression + "@:@" + upperExpression + "@]"
    );

    makeCase(
      text,
      substring(
        terminal("a", text.anchorIndex(0)),
        token(InfixOperator.SUBSCRIPTING, text.anchorIndex(1)),
        lowerBound == null ? null : terminal(lowerBound, text.anchorIndex(2)),
        token(Punctuation.COLON, text.anchorIndex(3)),
        upperBound == null ? null : terminal(upperBound, text.anchorIndex(4)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(5))
      )
    );
  }

  @Test
  public void shouldParseSubscriptingOperator() {
    TextWithAnchors text = new TextWithAnchors(
      "@a@[@b@]"
    );

    makeCase(
      text,
      subscripting(
        terminal("a", text.anchorIndex(0)),
        token(InfixOperator.SUBSCRIPTING, text.anchorIndex(1)),
        terminal("b", text.anchorIndex(2)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(3))
      )
    );
  }

  @Test
  public void shouldParseBranchingOperator() {
    TextWithAnchors text = new TextWithAnchors(
      "@a @? @b @: @c"
    );

    makeCase(
      text,
      ifElse(
        terminal("a", text.anchorIndex(0)),
        token(InfixOperator.BRANCHING, text.anchorIndex(1)),
        terminal("b", text.anchorIndex(2)),
        token(Punctuation.COLON, text.anchorIndex(3)),
        terminal("c", text.anchorIndex(4))
      )
    );
  }

  @Test
  public void shouldParseArraySyntax() {
    TextWithAnchors text = new TextWithAnchors(
      "@[@5, @3, @'hello', @true, @false, @c@]"
    );

    makeCase(
      text,
      array(
        token(InfixOperator.SUBSCRIPTING, text.anchorIndex(0)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(7)),
        terminal(5, text.anchorIndex(1)),
        terminal(3, text.anchorIndex(2)),
        terminal("'hello'", text.anchorIndex(3)),
        terminal(true, text.anchorIndex(4)),
        terminal(false, text.anchorIndex(5)),
        terminal("c", text.anchorIndex(6))
      )
    );

    text = new TextWithAnchors(
      "@[@]"
    );

    makeCase(
      text,
      array(
        token(InfixOperator.SUBSCRIPTING, text.anchorIndex(0)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(1))
      )
    );
  }

  @Test
  public void shouldThrowOnTrailingExpressions() {
    Object[] trailingTokens = { "c", Punctuation.OPENING_PARENTHESIS, 5, true };

    for (Object trailingToken : trailingTokens) {
      TextWithAnchors text = new TextWithAnchors(
        "a + b @" + trailingToken
      );

      makeErrorCase(
        text,
        ExpressionParserError.EXPECTED_EOS,
        text.anchorIndex(0)
      );
    }
  }

  protected static ExpressionNode array(
    Token openingBracket,
    Token closingBracket,
    ExpressionNode... items
  ) {
    return new ArrayNode((InfixOperatorToken) openingBracket, Arrays.asList(items), (PunctuationToken) closingBracket);
  }

  protected static ExpressionNode ifElse(
    ExpressionNode condition,
    Token conditionSeparator,
    ExpressionNode branchTrue,
    Token branchSeparator,
    ExpressionNode branchFalse
  ) {
    return new IfElseNode(condition, (InfixOperatorToken) conditionSeparator, branchTrue, (PunctuationToken) branchSeparator, branchFalse);
  }

  protected static ExpressionNode subscripting(
    ExpressionNode lhs,
    Token openingBracket,
    ExpressionNode rhs,
    Token closingBracket
  ) {
    return new SubscriptingNode(lhs, (InfixOperatorToken) openingBracket, rhs, (PunctuationToken) closingBracket);
  }

  protected static ExpressionNode substring(
    ExpressionNode operand,
    Token openingBracket,
    @Nullable ExpressionNode lowerBound,
    Token boundsSeparator,
    @Nullable ExpressionNode upperBound,
    Token closingBracket
  ) {
    return new SubstringNode(
      operand,
      (InfixOperatorToken) openingBracket,
      lowerBound, (PunctuationToken) boundsSeparator, upperBound,
      (PunctuationToken) closingBracket
    );
  }

  protected static Token token(Object value, int beginIndex) {
    return ExpressionTokenizerTests.makeToken(value, beginIndex);
  }

  protected static TerminalNode terminal(Object value, int beginIndex) {
    Token token = ExpressionTokenizerTests.makeToken(value, beginIndex);

    if (!(token instanceof TerminalToken))
      throw new IllegalStateException("Provided non-terminal representing value");

    return new TerminalNode((TerminalToken) token);
  }

  protected static ExpressionNode prefix(ExpressionNode operand, PrefixOperator operator, int operatorBeginIndex) {
    return new PrefixOperationNode(new PrefixOperatorToken(operatorBeginIndex, operator), operand);
  }

  protected static ExpressionNode infix(ExpressionNode lhs, InfixOperator operator, int operatorBeginIndex, ExpressionNode rhs) {
    return new InfixOperationNode(lhs, new InfixOperatorToken(operatorBeginIndex, operator), rhs);
  }

  private void makeErrorCase(TextWithAnchors input, ExpressionParserError error, int charIndex) {
    ExpressionParserException thrownException = null;

    try {
      ExpressionParser.parse(input.text);
    } catch (ExpressionParserException exception) {
      thrownException = exception;
    }

    Assertions.assertNotNull(thrownException, "Expected an error to be thrown");
    Assertions.assertEquals(error, thrownException.error, "Encountered mismatching error-type");
    Assertions.assertEquals(charIndex, thrownException.charIndex);
  }

  private void makeCase(TextWithAnchors input, @Nullable ExpressionNode expectedNode) {
    ExpressionNode actualNode = ExpressionParser.parse(input.text);

    if (expectedNode == null) {
      Assertions.assertNull(actualNode, "Expected the parse-result to be null");
      return;
    }

    Assertions.assertNotNull(actualNode, "Expected the parse-result to be non-null");
    Assertions.assertEquals(expectedNode.toString(), actualNode.toString());
  }
}
