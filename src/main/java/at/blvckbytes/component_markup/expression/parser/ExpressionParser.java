package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizer;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

  private final ExpressionTokenizer tokenizer;
  private final @Nullable TokenOutput tokenOutput;

  private ExpressionParser(ExpressionTokenizer tokenizer, @Nullable TokenOutput tokenOutput) {
    this.tokenizer = tokenizer;
    this.tokenOutput = tokenOutput;
  }

  public static @Nullable ExpressionNode parse(StringView value, @Nullable TokenOutput tokenOutput) {
    ExpressionTokenizer tokenizer = new ExpressionTokenizer(value, tokenOutput);
    ExpressionParser parser = new ExpressionParser(tokenizer, tokenOutput);
    ExpressionNode result = parser.parseExpression(null);

    Token trailingToken;

    if ((trailingToken = tokenizer.peekToken()) != null)
      throw new ExpressionParseException(trailingToken.raw.startInclusive, ExpressionParserError.EXPECTED_EOS);

    return result;
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

    if (priorOperator != null) {
      if (upcomingOperator == priorOperator) {
        if (!upcomingOperator.rightAssociative)
          return lhs;
      }

      else if (upcomingOperator.precedence <= priorOperator.precedence)
        return lhs;
    }

    tokenizer.nextToken();

    ExpressionNode rhs = parseExpression(upcomingOperator);

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
    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.EXPRESSION__OPERATOR__ANY, terminationToken.raw);

    return new SubstringNode(operand, operatorToken, lowerBound, colonToken, upperBound, terminationToken);
  }

  private ExpressionNode makeInfixExpression(ExpressionNode lhs, InfixOperatorToken operatorToken, ExpressionNode rhs) {
    if (operatorToken.operator == InfixOperator.SUBSCRIPTING) {
      PunctuationToken delimiterToken = tokenizer.nextToken(PunctuationToken.class);

      if (delimiterToken == null)
        throw new ExpressionParseException(rhs.getEndExclusive() - 1, ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET);

      if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET) {
        // For visual consistency, it's considered a two-part operator
        if (tokenOutput != null)
          tokenOutput.emitToken(TokenType.EXPRESSION__OPERATOR__ANY, delimiterToken.raw);

        return new InfixOperationNode(lhs, operatorToken, rhs, delimiterToken);
      }

      if (delimiterToken.punctuation == Punctuation.COLON)
        return parseSubstringExpression(lhs, operatorToken, rhs, delimiterToken);

      throw new ExpressionParseException(delimiterToken.raw.startInclusive, ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET);
    }

    if (operatorToken.operator == InfixOperator.BRANCHING) {
      PunctuationToken delimiterToken = tokenizer.nextToken(PunctuationToken.class);

      if (delimiterToken == null)
        throw new ExpressionParseException(rhs.getEndExclusive() - 1, ExpressionParserError.EXPECTED_BRANCH_DELIMITER);

      if (delimiterToken.punctuation != Punctuation.COLON)
        throw new ExpressionParseException(delimiterToken.raw.startInclusive, ExpressionParserError.EXPECTED_BRANCH_DELIMITER);

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

    if (introductionToken == null)
      return parsePrefixExpression();

    if (introductionToken.operator != InfixOperator.SUBSCRIPTING)
      return parsePrefixExpression();

    // In this context, it's not really an operator
    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.EXPRESSION__PUNCTUATION__ANY, introductionToken.raw);

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
        throw new ExpressionParseException(arrayItem.getEndExclusive() - 1, ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET);

      if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET)
        break;

      if (delimiterToken.punctuation == Punctuation.COMMA) {
        tokenizer.nextToken();
        continue;
      }

      throw new ExpressionParseException(delimiterToken.raw.startInclusive, ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET);
    }

    PunctuationToken terminatorToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminatorToken == null) {
      int position;

      if (!arrayItems.isEmpty())
        position = arrayItems.get(arrayItems.size() - 1).getEndExclusive() - 1;
      else
        position = introductionToken.raw.endExclusive - 1;

      throw new ExpressionParseException(position, ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET);
    }

    if (terminatorToken.punctuation != Punctuation.CLOSING_BRACKET)
      throw new ExpressionParseException(terminatorToken.raw.startInclusive, ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET);

    return new ArrayNode(introductionToken, arrayItems, terminatorToken);
  }
  // ================================================================================
  // Prefix
  // ================================================================================

  private @Nullable ExpressionNode parsePrefixExpression() {
    PrefixOperatorToken operatorToken = tokenizer.peekToken(PrefixOperatorToken.class);

    if (operatorToken == null)
      return parseParenthesesExpression();

    tokenizer.nextToken();

    ExpressionNode operand = parsePrefixExpression();

    if (operand == null)
      throw new ExpressionParseException(operatorToken.raw.endExclusive - 1, ExpressionParserError.EXPECTED_PREFIX_OPERAND, operatorToken.operator.representation);

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
      throw new ExpressionParseException(expression.getEndExclusive() - 1, ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS);

    if (terminationToken.punctuation != Punctuation.CLOSING_PARENTHESIS)
      throw new ExpressionParseException(terminationToken.raw.startInclusive, ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS);

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
