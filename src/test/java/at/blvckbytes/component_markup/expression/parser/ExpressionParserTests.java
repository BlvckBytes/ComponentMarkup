package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizerTests;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.markup.xml.TextWithAnchors;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ExpressionParserTests {

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
      "@a + @5 - @c + @d"
    );

    makeCase(
      text,
      infix(
        infix(
          infix(
            terminal("a", text.anchorIndex(0)),
            InfixOperator.ADDITION,
            terminal(5, text.anchorIndex(1))
          ),
          InfixOperator.SUBTRACTION,
          terminal("c", text.anchorIndex(2))
        ),
        InfixOperator.ADDITION,
        terminal("d", text.anchorIndex(3))
      )
    );
  }

  @Test
  public void shouldParseClimbingPrecedences() {
    TextWithAnchors text = new TextWithAnchors(
      // (a + ((b * 3) * d))
      "@a + @b * @3 * @d"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.anchorIndex(0)),
        InfixOperator.ADDITION,
        infix(
          infix(
            terminal("b", text.anchorIndex(1)),
            InfixOperator.MULTIPLICATION,
            terminal(3, text.anchorIndex(2))
          ),
          InfixOperator.MULTIPLICATION,
          terminal("d", text.anchorIndex(3))
        )
      )
    );
  }

  @Test
  public void shouldParseFallingPrecedences() {
    TextWithAnchors text = new TextWithAnchors(
      // (((a ^ 2) * c) + d)
      "@a ^ @2 * @c + @d"
    );

    makeCase(
      text,
      infix(
        infix(
          infix(
            terminal("a", text.anchorIndex(0)),
            InfixOperator.EXPONENTIATION,
            terminal(2, text.anchorIndex(1))
          ),
          InfixOperator.MULTIPLICATION,
          terminal("c", text.anchorIndex(2))
        ),
        InfixOperator.ADDITION,
        terminal("d", text.anchorIndex(3))
      )
    );
  }

  @Test
  public void shouldParseAlternatingPrecedences() {
    TextWithAnchors text = new TextWithAnchors(
      // ((a + (5 * c)) - ((d / 2) % x))
      "@a + @5 * @c - @d / @2 % @x"
    );

    makeCase(
      text,
      infix(
        infix(
          terminal("a", text.anchorIndex(0)),
          InfixOperator.ADDITION,
          infix(
            terminal(5, text.anchorIndex(1)),
            InfixOperator.MULTIPLICATION,
            terminal("c", text.anchorIndex(2))
          )
        ),
        InfixOperator.SUBTRACTION,
        infix(
          infix(
            terminal("d", text.anchorIndex(3)),
            InfixOperator.DIVISION,
            terminal(2, text.anchorIndex(4))
          ),
          InfixOperator.MODULO,
          terminal("x", text.anchorIndex(5))
        )
      )
    );
  }

  @Test
  public void shouldParseExponentiationWithRightAssociativity() {
    TextWithAnchors text = new TextWithAnchors(
      // (a ^ (b ^ (c ^ d)))
      "@a ^ @b ^ @c ^ @d"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.anchorIndex(0)),
        InfixOperator.EXPONENTIATION,
        infix(
          terminal("b", text.anchorIndex(1)),
          InfixOperator.EXPONENTIATION,
          infix(
            terminal("c", text.anchorIndex(2)),
            InfixOperator.EXPONENTIATION,
            terminal("d", text.anchorIndex(3))
          )
        )
      )
    );
  }

  @Test
  public void shouldRespectParentheses() {
    TextWithAnchors text = new TextWithAnchors(
      // ((a + b) * (c - d))
      "(@a + @b) * (@c - @d)"
    );

    makeCase(
      text,
      infix(
        parenthesised(
          infix(
            terminal("a", text.anchorIndex(0)),
            InfixOperator.ADDITION,
            terminal("b", text.anchorIndex(1))
          )
        ),
        InfixOperator.MULTIPLICATION,
        parenthesised(
          infix(
            terminal("c", text.anchorIndex(2)),
            InfixOperator.SUBTRACTION,
            terminal("d", text.anchorIndex(3))
          )
        )
      )
    );
  }

  @Test
  public void shouldParsePrefixOperations() {
    TextWithAnchors text = new TextWithAnchors(
      "@25 * @-@3"
    );

    makeCase(
      text,
      infix(
        terminal(25, text.anchorIndex(0)),
        InfixOperator.MULTIPLICATION,
        prefix(
          terminal(3, text.anchorIndex(2)),
          PrefixOperator.FLIP_SIGN,
          text.anchorIndex(1)
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
      "@a && @!@b"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.anchorIndex(0)),
        InfixOperator.CONJUNCTION,
        prefix(
          terminal("b", text.anchorIndex(2)),
          PrefixOperator.NEGATION,
          text.anchorIndex(1)
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
      "@a[@b@]"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.anchorIndex(0)),
        InfixOperator.SUBSCRIPTING,
        terminal("b", text.anchorIndex(1)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(2))
      )
    );
  }

  @Test
  public void shouldParseBranchingOperator() {
    TextWithAnchors text = new TextWithAnchors(
      "@a ? @b : @c"
    );

    makeCase(
      text,
      branching(
        terminal("a", text.anchorIndex(0)),
        terminal("b", text.anchorIndex(1)),
        terminal("c", text.anchorIndex(2))
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
  public void shouldSubscriptIntoImmediateArray() {
    TextWithAnchors text = new TextWithAnchors(
      "@[@0, @1, @2@][@0@]"
    );

    makeCase(
      text,
      infix(
        array(
          token(InfixOperator.SUBSCRIPTING, text.anchorIndex(0)),
          token(Punctuation.CLOSING_BRACKET, text.anchorIndex(4)),
          terminal(0, text.anchorIndex(1)),
          terminal(1, text.anchorIndex(2)),
          terminal(2, text.anchorIndex(3))
        ),
        InfixOperator.SUBSCRIPTING,
        terminal(0, text.anchorIndex(5)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(6))
      )
    );
  }

  @Test
  public void shouldParseNestedArrays() {
    TextWithAnchors text = new TextWithAnchors(
      "@[@[@0, @1@], @[@2, @3@], @[@4, @5@]@][@0@][@1@]"
    );

    makeCase(
      text,
      infix(
        infix(
          array(
            token(InfixOperator.SUBSCRIPTING, text.anchorIndex(0)),
            token(Punctuation.CLOSING_BRACKET, text.anchorIndex(13)),
            array(
              token(InfixOperator.SUBSCRIPTING, text.anchorIndex(1)),
              token(Punctuation.CLOSING_BRACKET, text.anchorIndex(4)),
              terminal(0, text.anchorIndex(2)),
              terminal(1, text.anchorIndex(3))
            ),
            array(
              token(InfixOperator.SUBSCRIPTING, text.anchorIndex(5)),
              token(Punctuation.CLOSING_BRACKET, text.anchorIndex(8)),
              terminal(2, text.anchorIndex(6)),
              terminal(3, text.anchorIndex(7))
            ),
            array(
              token(InfixOperator.SUBSCRIPTING, text.anchorIndex(9)),
              token(Punctuation.CLOSING_BRACKET, text.anchorIndex(12)),
              terminal(4, text.anchorIndex(10)),
              terminal(5, text.anchorIndex(11))
            )
          ),
          InfixOperator.SUBSCRIPTING,
          terminal(0, text.anchorIndex(14)),
          token(Punctuation.CLOSING_BRACKET, text.anchorIndex(15))
        ),
        InfixOperator.SUBSCRIPTING,
        terminal(1, text.anchorIndex(16)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(17))
      )
    );
  }

  @Test
  public void shouldSubscriptIntoSingleItemArray() {
    TextWithAnchors text = new TextWithAnchors(
      "@[@0@][@0@]"
    );

    makeCase(
      text,
      infix(
        array(
          token(InfixOperator.SUBSCRIPTING, text.anchorIndex(0)),
          token(Punctuation.CLOSING_BRACKET, text.anchorIndex(2)),
          terminal(0, text.anchorIndex(1))
        ),
        InfixOperator.SUBSCRIPTING,
        terminal(0, text.anchorIndex(3)),
        token(Punctuation.CLOSING_BRACKET, text.anchorIndex(4))
      )
    );
  }

  private final PrefixOperator[][] prefixCases = {
    { PrefixOperator.FLIP_SIGN },
    { PrefixOperator.FLIP_SIGN, PrefixOperator.FLIP_SIGN },
    { PrefixOperator.FLIP_SIGN, PrefixOperator.NEGATION, PrefixOperator.FLIP_SIGN },
    { PrefixOperator.NEGATION, PrefixOperator.NEGATION },
  };

  @Test
  public void shouldParsePrefixWithMemberAccess() {
    for (PrefixOperator[] prefixCase : prefixCases) {
      TextWithAnchors text = new TextWithAnchors(
        joinAtPrependedPrefixes(prefixCase) + "@a@.@m_b@.@m_c"
      );

      makeCase(
        text,
        makePrefixMemberSubscriptExpression(
          text,
          prefixCase,
          new String[] { "a", "m_b", "m_c" }
        )
      );
    }
  }

  @Test
  public void shouldParsePrefixWithSubscripting() {
    for (PrefixOperator[] prefixCase : prefixCases) {
      TextWithAnchors text = new TextWithAnchors(
        joinAtPrependedPrefixes(prefixCase) + "@a@[@s_b@]@[@s_c@]"
      );

      makeCase(
        text,
        makePrefixMemberSubscriptExpression(
          text,
          prefixCase,
          new String[] { "a", "s_b", "s_c" }
        )
      );
    }
  }

  @Test
  public void shouldParsePrefixWithMemberAccessAndSubscripting() {
    for (PrefixOperator[] prefixCase : prefixCases) {
      TextWithAnchors text = new TextWithAnchors(
        joinAtPrependedPrefixes(prefixCase) + "@a@[@s_b@]@.@m_c@[@s_d@]@.@m_e@[@s_f@]"
      );

      makeCase(
        text,
        makePrefixMemberSubscriptExpression(
          text,
          prefixCase,
          new String[] { "a", "s_b", "m_c", "s_d", "m_e", "s_f" }
        )
      );
    }
  }

  private ExpressionNode makePrefixMemberSubscriptExpression(
    TextWithAnchors text,
    PrefixOperator[] prefixes,
    String[] identifiers
  ) {
    ExpressionNode node = MemberAndSubscriptChainGenerator.generate(
      text,
      prefixes.length,
      Arrays.asList(identifiers)
    );

    for (int i = prefixes.length - 1; i >= 0; --i)
      node = prefix(node, prefixes[i], text.anchorIndex(i));

    return node;
  }

  private String joinAtPrependedPrefixes(PrefixOperator[] prefixes) {
    StringBuilder result = new StringBuilder();

    for (PrefixOperator prefix : prefixes)
      result.append('@').append(prefix);

    return result.toString();
  }

  @Test
  public void shouldParsePrefixWithInfixOpRightBeforeSubscripting() {
    for (PrefixOperator[] prefixCase : prefixCases) {
      TextWithAnchors text = new TextWithAnchors(
        joinAtPrependedPrefixes(prefixCase) + "@a ?? @b"
      );

      int indexOffset = prefixCase.length;

      ExpressionNode lhs = terminal("a", text.anchorIndex(indexOffset));

      for (int i = prefixCase.length - 1; i >= 0; --i)
        lhs = prefix(lhs, prefixCase[i], text.anchorIndex(i));

      makeCase(
        text,
        infix(
          lhs,
          InfixOperator.FALLBACK,
          terminal("b", text.anchorIndex(indexOffset + 1))
        )
      );
    }
  }

  @Test
  public void shouldConvertNodesToExpressions() {
    makeCasePlain("5 + 4");
    makeCasePlain("a[b].c");
    makeCasePlain("d && (f || g)");
    makeCasePlain(".23 - .4 & 'hello'");
    makeCasePlain("a[:]");
    makeCasePlain("a[b:]");
    makeCasePlain("a[:b]");
    makeCasePlain("a[c:b]");
    makeCasePlain("a ? b : c");
    makeCasePlain("[a, b, c]");
    makeCasePlain("[]");
  }

  @Test
  public void shouldPassComplicatedPlainTests() {
    // Because honestly, I'm too lazy to depict the whole AST...

    makeCasePlain("a + --b * c & --a.b[c].d");
  }

  protected static ExpressionNode array(
    Token openingBracket,
    Token closingBracket,
    ExpressionNode... items
  ) {
    return new ArrayNode((InfixOperatorToken) openingBracket, Arrays.asList(items), (PunctuationToken) closingBracket);
  }

  protected static ExpressionNode branching(
    ExpressionNode condition,
    ExpressionNode branchTrue,
    ExpressionNode branchFalse
  ) {
    return new BranchingNode(condition, branchTrue, branchFalse);
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

  protected static ExpressionNode parenthesised(ExpressionNode node) {
    node.parenthesised = true;
    return node;
  }

  protected static ExpressionNode infix(ExpressionNode lhs, InfixOperator operator, ExpressionNode rhs) {
    return infix(lhs, operator, rhs, null);
  }

  protected static ExpressionNode infix(ExpressionNode lhs, InfixOperator operator, ExpressionNode rhs, @Nullable Token terminator) {
    return new InfixOperationNode(lhs, operator, rhs, (PunctuationToken) terminator);
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

  private void makeCasePlain(String expression) {
    ExpressionNode actualNode = ExpressionParser.parse(expression);

    Assertions.assertNotNull(actualNode, "Expected the parse-result to be non-null");
    Assertions.assertEquals(expression, actualNode.toExpression());
  }
}
