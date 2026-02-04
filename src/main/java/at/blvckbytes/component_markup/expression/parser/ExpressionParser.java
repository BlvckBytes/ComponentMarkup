/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.*;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExpressionParser {

  private final ExpressionTokenizer tokenizer;

  private ExpressionParser(ExpressionTokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  public static @Nullable ExpressionNode parse(InputView input, @Nullable TokenOutput tokenOutput) {
    return parse(new ExpressionTokenizer(input, tokenOutput));
  }

  public static @Nullable ExpressionNode parse(ExpressionTokenizer tokenizer) {
    try {
      ExpressionParser parser = new ExpressionParser(tokenizer);
      ExpressionNode result = parser.parseExpression(null);

      Token trailingToken;

      if ((trailingToken = tokenizer.peekToken()) != null)
        throw new ExpressionParseException(trailingToken.raw.startInclusive, ExpressionParserError.EXPECTED_EOS);

      return result;
    } catch (ExpressionParseException e) {
      throw e.setRootView(tokenizer.input);
    }
  }

  private ExpressionNode patchInfixIfApplicable(InfixOperationNode node) {
    if (node.operatorToken.operator.precedence < InfixOperator.SUBSCRIPTING.precedence)
      return node;

    if (node.lhs instanceof InfixOperationNode)
      node.lhs = patchInfixIfApplicable((InfixOperationNode) node.lhs);

    if (node.lhs instanceof PrefixOperationNode) {
      PrefixOperationNode prefixOperation = (PrefixOperationNode) node.lhs;

      node.lhs = prefixOperation.operand;
      prefixOperation.operand = patchInfixIfApplicable(node);

      return prefixOperation;
    }

    return node;
  }

  private @Nullable ExpressionNode parseExpression(@Nullable InfixOperator priorOperator) {
    ExpressionNode lhs = parseArrayExpression();

    if (lhs == null)
      return null;

    while (true) {
      ExpressionNode node = parseInfixExpression(lhs, priorOperator);

      if (node == lhs) {
        if (priorOperator == null && node instanceof InfixOperationNode)
          lhs = patchInfixIfApplicable((InfixOperationNode) node);

        break;
      }

      lhs = node;
    }

    return lhs;
  }

  // ================================================================================
  // Infix
  // ================================================================================

  private @Nullable ExpressionNode parseInfixExpression(ExpressionNode lhs, @Nullable InfixOperator priorOperator) {
    InfixOperatorToken upcomingToken = tokenizer.peekToken(InfixOperatorToken.class);

    if (upcomingToken == null)
      return lhs;

    InfixOperator upcomingOperator = upcomingToken.operator;

    if (upcomingOperator == InfixOperator.BRANCHING_ELSE)
      return lhs;

    if (priorOperator != null) {
      if (upcomingOperator == priorOperator) {
        if (!upcomingOperator.flags.contains(OperatorFlag.RIGHT_ASSOCIATIVE))
          return lhs;
      }

      else if (upcomingOperator.precedence <= priorOperator.precedence)
        return lhs;
    }

    tokenizer.nextToken();

    ExpressionNode rhs = parseExpression(upcomingOperator == InfixOperator.SUBSCRIPTING ? null : upcomingOperator);

    if (rhs == null) {
      if (upcomingOperator == InfixOperator.SUBSCRIPTING) {
        if ((rhs = parseSubstringExpression(lhs, upcomingToken, null, null)) != null)
          return rhs;
      }

      throw new ExpressionParseException(upcomingToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_RIGHT_INFIX_OPERAND, upcomingOperator.representation);
    }

    return makeInfixExpression(lhs, upcomingToken, rhs);
  }

  private @Nullable ExpressionNode parseSubstringExpression(
    ExpressionNode operand,
    InfixOperatorToken operatorToken,
    @Nullable ExpressionNode lowerBound,
    @Nullable PunctuationToken colonToken
  ) {
    if (colonToken == null) {
      colonToken = tokenizer.peekToken(PunctuationToken.class);

      if (colonToken == null || colonToken.punctuation != Punctuation.COLON)
        return null;

      tokenizer.nextToken();
    }

    PunctuationToken terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken != null) {
      if (terminationToken.punctuation != Punctuation.CLOSING_BRACKET)
        throw new ExpressionParseException(terminationToken.raw.startInclusive, ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND);

      return new SubstringNode(operand, operatorToken, lowerBound, colonToken, null, terminationToken);
    }

    ExpressionNode upperBound = parseExpression(null);

    if (upperBound == null)
      throw new ExpressionParseException(colonToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND);

    terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken == null)
      throw new ExpressionParseException(upperBound.getEndExclusive() - 1, ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET);

    if (terminationToken.punctuation != Punctuation.CLOSING_BRACKET)
      throw new ExpressionParseException(terminationToken.raw.startInclusive, ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET);

    // For visual consistency, it's considered a two-part operator
    if (tokenizer.tokenOutput != null)
      tokenizer.tokenOutput.emitToken(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, terminationToken.raw);

    return new SubstringNode(operand, operatorToken, lowerBound, colonToken, upperBound, terminationToken);
  }

  private ExpressionNode makeInfixExpression(ExpressionNode lhs, InfixOperatorToken operatorToken, ExpressionNode rhs) {
    if (operatorToken.operator == InfixOperator.SUBSCRIPTING) {
      PunctuationToken delimiterToken = tokenizer.nextToken(PunctuationToken.class);

      if (delimiterToken == null)
        throw new ExpressionParseException(rhs.getEndExclusive() - 1, ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET);

      if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET) {
        // For visual consistency, it's considered a two-part operator
        if (tokenizer.tokenOutput != null)
          tokenizer.tokenOutput.emitToken(TokenType.EXPRESSION__SYMBOLIC_OPERATOR__ANY, delimiterToken.raw);

        return new InfixOperationNode(lhs, operatorToken, rhs, delimiterToken);
      }

      if (delimiterToken.punctuation == Punctuation.COLON)
        return parseSubstringExpression(lhs, operatorToken, rhs, delimiterToken);

      throw new ExpressionParseException(delimiterToken.raw.startInclusive, ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET);
    }

    if (operatorToken.operator == InfixOperator.BRANCHING_THEN) {
      InfixOperatorToken delimiterToken = tokenizer.peekToken(InfixOperatorToken.class);

      if (delimiterToken == null || delimiterToken.operator != InfixOperator.BRANCHING_ELSE)
        return new BranchingNode(lhs, operatorToken, rhs, null, null);

      tokenizer.nextToken();

      ExpressionNode falseBranch = parseExpression(null);

      if (falseBranch == null)
        throw new ExpressionParseException(delimiterToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_FALSE_BRANCH);

      return new BranchingNode(lhs, operatorToken, rhs, delimiterToken, falseBranch);
    }

    if (operatorToken.operator == InfixOperator.MEMBER) {
      if (!(rhs instanceof TerminalNode) || !(((TerminalNode) rhs).token instanceof IdentifierToken))
        throw new ExpressionParseException(rhs.getStartInclusive(), ExpressionParserError.EXPECTED_MEMBER_ACCESS_IDENTIFIER_RHS, lhs.toExpression());
    }

    return new InfixOperationNode(lhs, operatorToken, rhs, null);
  }

  // ================================================================================
  // Array
  // ================================================================================

  private @Nullable ExpressionNode parseArrayExpression() {
    InfixOperatorToken introductionToken = tokenizer.peekToken(InfixOperatorToken.class);

    if (introductionToken == null || introductionToken.operator != InfixOperator.SUBSCRIPTING)
      return parseMapExpression();

    // In this context, it's not really an operator
    if (tokenizer.tokenOutput != null)
      tokenizer.tokenOutput.emitToken(TokenType.EXPRESSION__PUNCTUATION__ANY, introductionToken.raw);

    tokenizer.nextToken();

    List<ExpressionNode> arrayItems = new ArrayList<>();

    PunctuationToken delimiterToken = null;

    while (true) {
      ExpressionNode arrayItem = parseExpression(null);

      if (arrayItem == null) {
        if (arrayItems.isEmpty())
          break;

        throw new ExpressionParseException(delimiterToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_ARRAY_ITEM);
      }

      arrayItems.add(arrayItem);

      delimiterToken = tokenizer.peekToken(PunctuationToken.class);

      if (delimiterToken == null)
        throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET);

      if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET)
        break;

      if (delimiterToken.punctuation == Punctuation.COMMA) {
        tokenizer.nextToken();
        continue;
      }

      throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET);
    }

    PunctuationToken terminatorToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminatorToken == null)
      throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET);

    if (terminatorToken.punctuation != Punctuation.CLOSING_BRACKET)
      throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_ARRAY_CLOSING_BRACKET);

    return new ArrayNode(introductionToken, arrayItems, terminatorToken);
  }

  // ================================================================================
  // Map
  // ================================================================================

  private @Nullable PunctuationToken peekPossibleClosingCurly() {
    tokenizer.allowPeekingClosingCurly = true;
    PunctuationToken introductionToken = tokenizer.peekToken(PunctuationToken.class);
    tokenizer.allowPeekingClosingCurly = false;
    return introductionToken;
  }

  private @Nullable ExpressionNode parseMapExpression() {
    PunctuationToken introductionToken = tokenizer.peekToken(PunctuationToken.class);

    if (introductionToken == null || introductionToken.punctuation != Punctuation.OPENING_CURLY)
      return parsePrefixExpression();

    tokenizer.nextToken();

    MapNodeItems mapItems = new MapNodeItems();
    ExpressionNode lastMapItem;

    PunctuationToken delimiterToken = null;

    while (true) {
      TerminalToken upcomingTerminal = tokenizer.peekToken(TerminalToken.class);

      if (!(upcomingTerminal instanceof StringToken || upcomingTerminal instanceof IdentifierToken)) {
        if (mapItems.isEmpty() || delimiterToken == null)
          break;

        throw new ExpressionParseException(delimiterToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_MAP_KEY);
      }

      tokenizer.nextToken();

      String mapKey = (String) upcomingTerminal.getPlainValue();

      if ((delimiterToken = peekPossibleClosingCurly()) == null)
        break;

      // Shorthand for equal key-name and value-identifier
      if (delimiterToken.punctuation == Punctuation.CLOSING_CURLY || delimiterToken.punctuation == Punctuation.COMMA) {
        lastMapItem = new TerminalNode(upcomingTerminal);
        mapItems.put(mapKey, lastMapItem);

        if (delimiterToken.punctuation == Punctuation.CLOSING_CURLY)
          break;

        tokenizer.nextToken(); // ,
        continue;
      }

      if (delimiterToken.punctuation == Punctuation.COLON) {
        tokenizer.nextToken(); // :

        lastMapItem = parseExpression(null);

        if (lastMapItem == null)
          throw new ExpressionParseException(delimiterToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_MAP_VALUE);

        mapItems.put(mapKey, lastMapItem);
      }

      delimiterToken = peekPossibleClosingCurly();

      if (delimiterToken == null || delimiterToken.punctuation == Punctuation.CLOSING_CURLY)
        break;

      if (delimiterToken.punctuation != Punctuation.COMMA)
        break;

      tokenizer.nextToken(); // ,
    }

    PunctuationToken terminatorToken = peekPossibleClosingCurly();

    if (terminatorToken == null)
      throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_MAP_CLOSING_CURLY);

    if (terminatorToken.punctuation != Punctuation.CLOSING_CURLY)
      throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_MAP_CLOSING_CURLY);

    tokenizer.nextToken();

    return new MapNode(introductionToken, mapItems, terminatorToken);
  }

  // ================================================================================
  // Prefix
  // ================================================================================

  private @Nullable ExpressionNode parsePrefixExpression() {
    Token upcomingToken = tokenizer.peekToken();

    PrefixOperatorToken operatorToken;
    PunctuationToken openingParenthesisToken = null;

    if (!(upcomingToken instanceof PrefixOperatorToken)) {
      if (!(upcomingToken instanceof IdentifierToken))
        return parseParenthesesExpression();

      PrefixOperator namedOperator = PrefixOperator.byName(((IdentifierToken) upcomingToken).identifier);

      if (namedOperator == null)
        return parseParenthesesExpression();

      tokenizer.nextToken();

      if (!namedOperator.flags.contains(OperatorFlag.PARENS))
        throw new IllegalStateException("Named non-parenthesised prefix-operators should've been taken care of in the tokenizer");

      PunctuationToken punctuationToken = tokenizer.peekToken(PunctuationToken.class);

      if (punctuationToken == null || punctuationToken.punctuation != Punctuation.OPENING_PARENTHESIS) {
        tokenizer.putBackToken(upcomingToken);
        return parseParenthesesExpression();
      }

      tokenizer.nextToken();

      if (tokenizer.tokenOutput != null)
        tokenizer.tokenOutput.emitToken(TokenType.EXPRESSION__NAMED_PREFIX_OPERATOR, upcomingToken.raw);

      operatorToken = new PrefixOperatorToken(upcomingToken.raw, namedOperator);
      openingParenthesisToken = punctuationToken;
    }

    else {
      tokenizer.nextToken();
      operatorToken = (PrefixOperatorToken) upcomingToken;
    }

    boolean hasParens = operatorToken.operator.flags.contains(OperatorFlag.PARENS);
    ExpressionNode operand = hasParens ? parseExpression(null) : parseArrayExpression();

    if (operand == null) {
      throw new ExpressionParseException(
        openingParenthesisToken == null
          ? operatorToken.raw.endExclusive - 1
          : openingParenthesisToken.raw.startInclusive,
        ExpressionParserError.EXPECTED_PREFIX_OPERAND,
        operatorToken.operator.representation
      );
    }

    if (openingParenthesisToken != null) {
      PunctuationToken punctuationToken;

      boolean isVariadic = operatorToken.operator.flags.contains(OperatorFlag.VARIADIC);

      List<ExpressionNode> operands = null;

      if (isVariadic) {
        operands = new ArrayList<>();
        operands.add(operand);
      }

      while ((punctuationToken = tokenizer.peekToken(PunctuationToken.class)) != null) {
        if (punctuationToken.punctuation != Punctuation.COMMA)
          break;

        if (!isVariadic) {
          throw new ExpressionParseException(
            punctuationToken.raw.startInclusive,
            ExpressionParserError.NON_VARIADIC_PREFIX_OPERATOR,
            operatorToken.operator.representation);
        }

        tokenizer.nextToken();

        ExpressionNode nextOperand = parseArrayExpression();

        if (nextOperand == null) {
          throw new ExpressionParseException(
            punctuationToken.raw.startInclusive,
            ExpressionParserError.EXPECTED_VARIADIC_OPERAND,
            operatorToken.operator.representation);
        }

        operands.add(nextOperand);
      }

      punctuationToken = tokenizer.nextToken(PunctuationToken.class);

      if (punctuationToken == null || punctuationToken.punctuation != Punctuation.CLOSING_PARENTHESIS) {
        // Point at the end of the very last variadic operand
        if (operands != null)
          operand = operands.get(operands.size() - 1);

        throw new ExpressionParseException(
          operand.getLastMemberPositionProvider().endExclusive - 1,
          ExpressionParserError.EXPECTED_PREFIX_OPERAND_CLOSING_PARENTHESIS,
          operatorToken.operator.representation
        );
      }

      // Basically, I'm using the parens of operator() as a short-hand for operator([]) to
      // save on the explicit immediate array-syntax - same intent, more expressivity.
      if (operands != null)
        operand = new ArrayNode(openingParenthesisToken, operands, punctuationToken);
    }

    return new PrefixOperationNode(operatorToken, operand);
  }

  // ================================================================================
  // Parentheses
  // ================================================================================

  private @Nullable ExpressionNode parseParenthesesExpression() {
    PunctuationToken introductionToken = tokenizer.peekToken(PunctuationToken.class);

    if (introductionToken == null)
      return parseTerminalNode();

    if (introductionToken.punctuation != Punctuation.OPENING_PARENTHESIS)
      return parseTerminalNode();

    tokenizer.nextToken();

    ExpressionNode expression = parseExpression(null);

    if (expression == null)
      throw new ExpressionParseException(introductionToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_PARENTHESES_CONTENT);

    PunctuationToken terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken == null)
      throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_CLOSING_PARENTHESIS);

    if (terminationToken.punctuation != Punctuation.CLOSING_PARENTHESIS)
      throw new ExpressionParseException(introductionToken.raw.startInclusive, ExpressionParserError.MISSING_CLOSING_PARENTHESIS);

    expression.parenthesised = true;

    return expression;
  }

  // ================================================================================
  // Terminal
  // ================================================================================

  private @Nullable ExpressionNode parseTerminalNode() {
    TerminalToken terminalToken = tokenizer.peekToken(TerminalToken.class);

    if (terminalToken == null)
      return null;

    tokenizer.nextToken();
    return new TerminalNode(terminalToken);
  }
}
