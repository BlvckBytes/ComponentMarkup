/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.tokenizer.*;
import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.test_utils.Jsonifier;
import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionParserTests {

  @Test
  public void shouldParseEmptyInputAsNull() {
    TextWithSubViews text = new TextWithSubViews(
      ""
    );

    makeCase(
      text,
      null
    );
  }

  @Test
  public void shouldParseEqualPrecedences() {
    TextWithSubViews text = new TextWithSubViews(
      // (((a + 5) - c) + d)
      "`a´ `+´ `5´ `-´ `c´ `+´ `d´"
    );

    makeCase(
      text,
      infix(
        infix(
          infix(
            terminal("a", text.subView(0)),
            token(InfixOperator.ADDITION, text.subView(1)),
            terminal(5, text.subView(2))
          ),
          token(InfixOperator.SUBTRACTION, text.subView(3)),
          terminal("c", text.subView(4))
        ),
        token(InfixOperator.ADDITION, text.subView(5)),
        terminal("d", text.subView(6))
      )
    );
  }

  @Test
  public void shouldParseClimbingPrecedences() {
    TextWithSubViews text = new TextWithSubViews(
      // (a + ((b * 3) * d))
      "`a´ `+´ `b´ `*´ `3´ `*´ `d´"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.subView(0)),
        token(InfixOperator.ADDITION, text.subView(1)),
        infix(
          infix(
            terminal("b", text.subView(2)),
            token(InfixOperator.MULTIPLICATION, text.subView(3)),
            terminal(3, text.subView(4))
          ),
          token(InfixOperator.MULTIPLICATION, text.subView(5)),
          terminal("d", text.subView(6))
        )
      )
    );
  }

  @Test
  public void shouldParseFallingPrecedences() {
    TextWithSubViews text = new TextWithSubViews(
      // (((a ^ 2) * c) + d)
      "`a´ `^´ `2´ `*´ `c´ `+´ `d´"
    );

    makeCase(
      text,
      infix(
        infix(
          infix(
            terminal("a", text.subView(0)),
            token(InfixOperator.EXPONENTIATION, text.subView(1)),
            terminal(2, text.subView(2))
          ),
          token(InfixOperator.MULTIPLICATION, text.subView(3)),
          terminal("c", text.subView(4))
        ),
        token(InfixOperator.ADDITION, text.subView(5)),
        terminal("d", text.subView(6))
      )
    );
  }

  @Test
  public void shouldParseAlternatingPrecedences() {
    TextWithSubViews text = new TextWithSubViews(
      // ((a + (5 * c)) - ((d / 2) % x))
      "`a´ `+´ `5´ `*´ `c´ `-´ `d´ `/´ `2´ `%´ `x´"
    );

    makeCase(
      text,
      infix(
        infix(
          terminal("a", text.subView(0)),
          token(InfixOperator.ADDITION, text.subView(1)),
          infix(
            terminal(5, text.subView(2)),
            token(InfixOperator.MULTIPLICATION, text.subView(3)),
            terminal("c", text.subView(4))
          )
        ),
        token(InfixOperator.SUBTRACTION, text.subView(5)),
        infix(
          infix(
            terminal("d", text.subView(6)),
            token(InfixOperator.DIVISION, text.subView(7)),
            terminal(2, text.subView(8))
          ),
          token(InfixOperator.MODULO, text.subView(9)),
          terminal("x", text.subView(10))
        )
      )
    );
  }

  @Test
  public void shouldParseExponentiationWithRightAssociativity() {
    TextWithSubViews text = new TextWithSubViews(
      // (a ^ (b ^ (c ^ d)))
      "`a´ `^´ `b´ `^´ `c´ `^´ `d´"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.subView(0)),
        token(InfixOperator.EXPONENTIATION, text.subView(1)),
        infix(
          terminal("b", text.subView(2)),
          token(InfixOperator.EXPONENTIATION, text.subView(3)),
          infix(
            terminal("c", text.subView(4)),
            token(InfixOperator.EXPONENTIATION, text.subView(5)),
            terminal("d", text.subView(6))
          )
        )
      )
    );
  }

  @Test
  public void shouldRespectParentheses() {
    TextWithSubViews text = new TextWithSubViews(
      // ((a + b) * (c - d))
      "(`a´ `+´ `b´) `*´ (`c´ `-´ `d´)"
    );

    makeCase(
      text,
      infix(
        parenthesised(
          infix(
            terminal("a", text.subView(0)),
            token(InfixOperator.ADDITION, text.subView(1)),
            terminal("b", text.subView(2))
          )
        ),
        token(InfixOperator.MULTIPLICATION, text.subView(3)),
        parenthesised(
          infix(
            terminal("c", text.subView(4)),
            token(InfixOperator.SUBTRACTION, text.subView(5)),
            terminal("d", text.subView(6))
          )
        )
      )
    );
  }

  @Test
  public void shouldParsePrefixOperations() {
    TextWithSubViews text = new TextWithSubViews(
      "`25´ `*´ `-´`3´"
    );

    makeCase(
      text,
      infix(
        terminal(25, text.subView(0)),
        token(InfixOperator.MULTIPLICATION, text.subView(1)),
        prefix(
          token(PrefixOperator.FLIP_SIGN, text.subView(2)),
          terminal(3, text.subView(3))
        )
      )
    );

    text = new TextWithSubViews(
      "`-´`3´"
    );

    makeCase(
      text,
      prefix(
        token(PrefixOperator.FLIP_SIGN, text.subView(0)),
        terminal(3, text.subView(1))
      )
    );

    text = new TextWithSubViews(
      "`a´ `&&´ `!´`b´"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.subView(0)),
        token(InfixOperator.CONJUNCTION, text.subView(1)),
        prefix(
          token(PrefixOperator.NEGATION, text.subView(2)),
          terminal("b", text.subView(3))
        )
      )
    );

    text = new TextWithSubViews(
      "`!´`b´"
    );

    makeCase(
      text,
      prefix(
        token(PrefixOperator.NEGATION, text.subView(0)),
        terminal("b", text.subView(1))
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

    TextWithSubViews text = new TextWithSubViews(
      "`a´`[´`" + lowerExpression + "´`:´`" + upperExpression + "´`]´"
    );

    makeCase(
      text,
      substring(
        terminal("a", text.subView(0)),
        token(InfixOperator.SUBSCRIPTING, text.subView(1)),
        lowerBound == null ? null : terminal(lowerBound, text.subView(2)),
        token(Punctuation.COLON, text.subView(3)),
        upperBound == null ? null : terminal(upperBound, text.subView(4)),
        token(Punctuation.CLOSING_BRACKET, text.subView(5))
      )
    );
  }

  @Test
  public void shouldParseSubscriptingOperator() {
    TextWithSubViews text = new TextWithSubViews(
      "`a´`[´`b´`]´"
    );

    makeCase(
      text,
      infix(
        terminal("a", text.subView(0)),
        token(InfixOperator.SUBSCRIPTING, text.subView(1)),
        terminal("b", text.subView(2)),
        token(Punctuation.CLOSING_BRACKET, text.subView(3))
      )
    );
  }

  @Test
  public void shouldParseBranchingOperator() {
    TextWithSubViews text = new TextWithSubViews(
      "`a´ `?´ `b´ `:´ `c´"
    );

    makeCase(
      text,
      branching(
        terminal("a", text.subView(0)),
        token(InfixOperator.BRANCHING, text.subView(1)),
        terminal("b", text.subView(2)),
        token(Punctuation.COLON, text.subView(3)),
        terminal("c", text.subView(4))
      )
    );

    text = new TextWithSubViews(
      "`a´ `?´ `b´"
    );

    makeCase(
      text,
      branching(
        terminal("a", text.subView(0)),
        token(InfixOperator.BRANCHING, text.subView(1)),
        terminal("b", text.subView(2)),
        null, null
      )
    );
  }

  @Test
  public void shouldParseArraySyntax() {
    TextWithSubViews text = new TextWithSubViews(
      "`[´`5´, `3´, `'hello'´, `true´, `false´, `c´`]´"
    );

    makeCase(
      text,
      array(
        token(InfixOperator.SUBSCRIPTING, text.subView(0)),
        token(Punctuation.CLOSING_BRACKET, text.subView(7)),
        terminal(5, text.subView(1)),
        terminal(3, text.subView(2)),
        terminal("'hello'", text.subView(3)),
        terminal(true, text.subView(4)),
        terminal(false, text.subView(5)),
        terminal("c", text.subView(6))
      )
    );

    text = new TextWithSubViews(
      "`[´`]´"
    );

    makeCase(
      text,
      array(
        token(InfixOperator.SUBSCRIPTING, text.subView(0)),
        token(Punctuation.CLOSING_BRACKET, text.subView(1))
      )
    );
  }

  @Test
  public void shouldSubscriptIntoImmediateArray() {
    TextWithSubViews text = new TextWithSubViews(
      "`[´`0´, `1´, `2´`]´`[´`0´`]´"
    );

    makeCase(
      text,
      infix(
        array(
          token(InfixOperator.SUBSCRIPTING, text.subView(0)),
          token(Punctuation.CLOSING_BRACKET, text.subView(4)),
          terminal(0, text.subView(1)),
          terminal(1, text.subView(2)),
          terminal(2, text.subView(3))
        ),
        token(InfixOperator.SUBSCRIPTING, text.subView(5)),
        terminal(0, text.subView(6)),
        token(Punctuation.CLOSING_BRACKET, text.subView(7))
      )
    );
  }

  @Test
  public void shouldParseNestedArrays() {
    TextWithSubViews text = new TextWithSubViews(
      "`[´`[´`0´, `1´`]´, `[´`2´, `3´`]´, `[´`4´, `5´`]´`]´`[´`0´`]´`[´`1´`]´"
    );

    makeCase(
      text,
      infix(
        infix(
          array(
            token(InfixOperator.SUBSCRIPTING, text.subView(0)),
            token(Punctuation.CLOSING_BRACKET, text.subView(13)),
            array(
              token(InfixOperator.SUBSCRIPTING, text.subView(1)),
              token(Punctuation.CLOSING_BRACKET, text.subView(4)),
              terminal(0, text.subView(2)),
              terminal(1, text.subView(3))
            ),
            array(
              token(InfixOperator.SUBSCRIPTING, text.subView(5)),
              token(Punctuation.CLOSING_BRACKET, text.subView(8)),
              terminal(2, text.subView(6)),
              terminal(3, text.subView(7))
            ),
            array(
              token(InfixOperator.SUBSCRIPTING, text.subView(9)),
              token(Punctuation.CLOSING_BRACKET, text.subView(12)),
              terminal(4, text.subView(10)),
              terminal(5, text.subView(11))
            )
          ),
          token(InfixOperator.SUBSCRIPTING, text.subView(14)),
          terminal(0, text.subView(15)),
          token(Punctuation.CLOSING_BRACKET, text.subView(16))
        ),
        token(InfixOperator.SUBSCRIPTING, text.subView(17)),
        terminal(1, text.subView(18)),
        token(Punctuation.CLOSING_BRACKET, text.subView(19))
      )
    );
  }

  @Test
  public void shouldSubscriptIntoSingleItemArray() {
    TextWithSubViews text = new TextWithSubViews(
      "`[´`0´`]´`[´`0´`]´"
    );

    makeCase(
      text,
      infix(
        array(
          token(InfixOperator.SUBSCRIPTING, text.subView(0)),
          token(Punctuation.CLOSING_BRACKET, text.subView(2)),
          terminal(0, text.subView(1))
        ),
        token(InfixOperator.SUBSCRIPTING,text.subView(3)),
        terminal(0, text.subView(4)),
        token(Punctuation.CLOSING_BRACKET, text.subView(5))
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
      TextWithSubViews text = new TextWithSubViews(
        joinAtPrependedPrefixes(prefixCase) + "`a´`.´`m_b´`.´`m_c´"
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
      TextWithSubViews text = new TextWithSubViews(
        joinAtPrependedPrefixes(prefixCase) + "`a´`[´`s_b´`]´`[´`s_c´`]´"
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
      TextWithSubViews text = new TextWithSubViews(
        joinAtPrependedPrefixes(prefixCase) + "`a´`[´`s_b´`]´`.´`m_c´`[´`s_d´`]´`.´`m_e´`[´`s_f´`]´"
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

  @Test
  public void shouldParseStringInterpolation() {
    makeInterpolationCase("prefix ", " mid ", " suffix");
    makeInterpolationCase("prefix ", "mid ", " suffix");
    makeInterpolationCase("prefix ", "mid", " suffix");
    makeInterpolationCase(null, "mid", " suffix");
    makeInterpolationCase("prefix ", "mid", null);
    makeInterpolationCase(null, "mid", null);
    makeInterpolationCase("prefix", "mid", "suffix");
    makeInterpolationCase(null, "mid", "suffix");
    makeInterpolationCase("prefix", "mid", null);
    makeInterpolationCase("prefix", null, "suffix");
    makeInterpolationCase("prefix", " ", "suffix");
  }

  private void makeInterpolationCase(@Nullable String prefix, @Nullable String mid, @Nullable String suffix) {
    TextWithSubViews text = new TextWithSubViews(
      "`×`" + (prefix == null ? "" : ("`" + prefix + "´")) + "{`a´ `+´ `b´}" + (mid == null ? "" : ("`" + mid + "´")) + "{`b´}" + (suffix == null ? "" : ("`" + suffix + "´")) + "×`´"
    );

    List<InterpolationMember> members = new ArrayList<>();
    int viewIndex = 1;

    if (prefix != null)
      members.add(text.subView(viewIndex++));

    members.add(
      infix(
        terminal("a", text.subView(viewIndex++)),
        token(InfixOperator.ADDITION, text.subView(viewIndex++)),
        terminal("b", text.subView(viewIndex++))
      )
    );

    if (mid != null)
      members.add(text.subView(viewIndex++));

    members.add(terminal("b", text.subView(viewIndex++)));

    if (suffix != null)
      members.add(text.subView(viewIndex));

    makeCase(
      text,
      interpolation(text.subView(0), members)
    );
  }

  @Test
  public void shouldParseStringInterpolationWithoutInterpolations() {
    TextWithSubViews text = new TextWithSubViews(
      "`×``hello world! :)´×`´"
    );

    makeCase(
      text,
      interpolation(
        text.subView(0),
        text.subView(1))
    );
  }

  private ExpressionNode makePrefixMemberSubscriptExpression(
    TextWithSubViews text,
    PrefixOperator[] prefixes,
    String[] identifiers
  ) {
    ExpressionNode node = MemberAndSubscriptChainGenerator.generate(
      text,
      prefixes.length,
      Arrays.asList(identifiers)
    );

    for (int i = prefixes.length - 1; i >= 0; --i)
      node = prefix(token(prefixes[i], text.subView(i)), node);

    return node;
  }

  private String joinAtPrependedPrefixes(PrefixOperator[] prefixes) {
    StringBuilder result = new StringBuilder();

    for (PrefixOperator prefix : prefixes)
      result.append('`').append(prefix).append('´');

    return result.toString();
  }

  @Test
  public void shouldParsePrefixWithInfixOpRightBeforeSubscripting() {
    for (PrefixOperator[] prefixCase : prefixCases) {
      TextWithSubViews text = new TextWithSubViews(
        joinAtPrependedPrefixes(prefixCase) + "`a´ `??´ `b´"
      );

      int indexOffset = prefixCase.length;

      ExpressionNode lhs = terminal("a", text.subView(indexOffset));

      for (int i = prefixCase.length - 1; i >= 0; --i)
        lhs = prefix(token(prefixCase[i], text.subView(i)), lhs);

      makeCase(
        text,
        infix(
          lhs,
          token(InfixOperator.FALLBACK, text.subView(indexOffset + 1)),
          terminal("b", text.subView(indexOffset + 2))
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
    Token branchingOperator,
    ExpressionNode branchTrue,
    Token branchingSeparator,
    ExpressionNode branchFalse
  ) {
    return new BranchingNode(condition, (InfixOperatorToken) branchingOperator, branchTrue, (PunctuationToken) branchingSeparator, branchFalse);
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

  protected static Token token(Object value, StringView subView) {
    return ExpressionTokenizerTests.makeToken(value, subView);
  }

  protected static TerminalNode interpolation(StringView raw, List<InterpolationMember> members) {
    return new TerminalNode(new StringToken(raw, members));
  }

  protected static TerminalNode interpolation(StringView raw, InterpolationMember... members) {
    return new TerminalNode(new StringToken(raw, Arrays.asList(members)));
  }

  protected static TerminalNode terminal(Object value, StringView subView) {
    Token token = ExpressionTokenizerTests.makeToken(value, subView);

    if (!(token instanceof TerminalToken))
      throw new IllegalStateException("Provided non-terminal representing value");

    return new TerminalNode((TerminalToken) token);
  }

  protected static ExpressionNode prefix(Token operator, ExpressionNode operand) {
    return new PrefixOperationNode((PrefixOperatorToken) operator, operand);
  }

  protected static ExpressionNode parenthesised(ExpressionNode node) {
    node.parenthesised = true;
    return node;
  }

  protected static ExpressionNode infix(ExpressionNode lhs, Token operatorToken, ExpressionNode rhs) {
    return infix(lhs, operatorToken, rhs, null);
  }

  protected static ExpressionNode infix(ExpressionNode lhs, Token operatorToken, ExpressionNode rhs, @Nullable Token terminator) {
    return new InfixOperationNode(lhs, (InfixOperatorToken) operatorToken, rhs, (PunctuationToken) terminator);
  }

  private void makeCase(TextWithSubViews input, @Nullable ExpressionNode expectedNode) {
    ExpressionNode actualNode = ExpressionParser.parse(StringView.of(input.text), null);

    if (expectedNode == null) {
      Assertions.assertNull(actualNode, "Expected the parse-result to be null");
      return;
    }

    Assertions.assertNotNull(actualNode, "Expected the parse-result to be non-null");
    Assertions.assertEquals(Jsonifier.jsonify(expectedNode), Jsonifier.jsonify(actualNode));
  }

  private void makeCasePlain(String expression) {
    ExpressionNode actualNode = ExpressionParser.parse(StringView.of(expression), null);

    Assertions.assertNotNull(actualNode, "Expected the parse-result to be non-null");
    Assertions.assertEquals(expression, actualNode.toExpression());
  }
}
