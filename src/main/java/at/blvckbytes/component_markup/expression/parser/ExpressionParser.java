package at.blvckbytes.component_markup.expression.parser;

import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizer;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.Punctuation;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

  private final ExpressionTokenizer tokenizer;

  private ExpressionParser(ExpressionTokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  public static @Nullable ExpressionNode parse(String input) {
    ExpressionTokenizer tokenizer = new ExpressionTokenizer(input);
    ExpressionParser parser = new ExpressionParser(tokenizer);
    ExpressionNode result = parser.parseExpression(null);

    Token trailingToken;

    if ((trailingToken = tokenizer.peekToken()) != null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_EOS, trailingToken);

    return result;
  }

  private @Nullable ExpressionNode parseExpression(@Nullable InfixOperator priorOperator) {
    ExpressionNode lhs = parsePrefixExpression();

    if (lhs == null)
      return null;

    while (true) {
      ExpressionNode node = parseInfixExpression(lhs, priorOperator);

      if (node == lhs)
        break;

      lhs = node;
    }

    return lhs;
  }

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

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_RIGHT_INFIX_OPERAND, upcomingToken);
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
      colonToken = tokenizer.nextToken(PunctuationToken.class);

      if (colonToken == null || colonToken.punctuation != Punctuation.COLON)
        return null;
    }

    PunctuationToken terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken != null && terminationToken.punctuation == Punctuation.CLOSING_BRACKET)
      return new SubstringNode(operand, operatorToken, lowerBound, colonToken, null, terminationToken);

    ExpressionNode upperBound = parseExpression(null);

    if (upperBound == null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND, terminationToken);

    terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken != null && terminationToken.punctuation == Punctuation.CLOSING_BRACKET)
      return new SubstringNode(operand, operatorToken, lowerBound, colonToken, upperBound, terminationToken);

    throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET, terminationToken);
  }

  private ExpressionNode makeInfixExpression(ExpressionNode lhs, InfixOperatorToken operatorToken, ExpressionNode rhs) {
    if (operatorToken.operator == InfixOperator.SUBSCRIPTING) {
      PunctuationToken delimiterToken = tokenizer.nextToken(PunctuationToken.class);

      if (delimiterToken != null) {
        if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET)
          return new SubscriptingNode(lhs, operatorToken, rhs, delimiterToken);

        if (delimiterToken.punctuation == Punctuation.COLON)
          return parseSubstringExpression(lhs, operatorToken, rhs, delimiterToken);
      }

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET, delimiterToken);
    }

    if (operatorToken.operator == InfixOperator.BRANCHING) {
      PunctuationToken delimiterToken = tokenizer.nextToken(PunctuationToken.class);

      if (delimiterToken != null && delimiterToken.punctuation == Punctuation.COLON) {
        ExpressionNode falseBranch = parseExpression(null);

        if (falseBranch == null)
          throw new ExpressionParserException(ExpressionParserError.EXPECTED_FALSE_BRANCH, delimiterToken);

        return new IfElseNode(lhs, operatorToken, rhs, delimiterToken, falseBranch);
      }

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_BRANCH_DELIMITER, delimiterToken);
    }

    return new InfixOperationNode(lhs, operatorToken, rhs);
  }

  private @Nullable ExpressionNode parsePrefixExpression() {
    PrefixOperatorToken operatorToken = tokenizer.peekToken(PrefixOperatorToken.class);

    if (operatorToken == null)
      return parseArrayExpression();

    tokenizer.nextToken();

    ExpressionNode operand = parseExpression(null);

    if (operand == null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_PREFIX_OPERAND, operatorToken);

    return new PrefixOperationNode(operatorToken, operand);
  }

  private @Nullable ExpressionNode parseArrayExpression() {
    InfixOperatorToken introductionToken = tokenizer.peekToken(InfixOperatorToken.class);

    if (introductionToken == null)
      return parseParenthesesExpression();

    if (introductionToken.operator != InfixOperator.SUBSCRIPTING)
      return parseParenthesesExpression();

    tokenizer.nextToken();

    List<ExpressionNode> arrayItems = new ArrayList<>();

    PunctuationToken delimiterToken = null;

    while (tokenizer.peekToken() != null) {
      ExpressionNode arrayItem = parseExpression(null);

      if (arrayItem == null) {
        if (arrayItems.isEmpty())
          break;

        throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_ITEM, delimiterToken);
      }

      arrayItems.add(arrayItem);

      delimiterToken = tokenizer.peekToken(PunctuationToken.class);

      if (delimiterToken != null) {
        if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET)
          break;

        if (delimiterToken.punctuation == Punctuation.COMMA) {
          tokenizer.nextToken();
          continue;
        }
      }

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET, delimiterToken);
    }

    PunctuationToken terminatorToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminatorToken != null && terminatorToken.punctuation == Punctuation.CLOSING_BRACKET)
      return new ArrayNode(introductionToken, arrayItems, terminatorToken);

    throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET, terminatorToken);
  }

  private @Nullable ExpressionNode parseParenthesesExpression() {
    PunctuationToken introductionToken = tokenizer.peekToken(PunctuationToken.class);

    if (introductionToken == null)
      return parseTerminalNode();

    if (introductionToken.punctuation != Punctuation.OPENING_PARENTHESIS)
      return parseTerminalNode();

    tokenizer.nextToken();

    ExpressionNode expression = parseExpression(null);

    if (expression == null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_PARENTHESES_CONTENT, introductionToken);

    PunctuationToken terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken != null && terminationToken.punctuation == Punctuation.CLOSING_PARENTHESIS)
      return expression;

    throw new ExpressionParserException(ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS, terminationToken);
  }

  private @Nullable ExpressionNode parseTerminalNode() {
    TerminalToken terminalToken = tokenizer.peekToken(TerminalToken.class);

    if (terminalToken == null)
      return null;

    tokenizer.nextToken();
    return new TerminalNode(terminalToken);
  }
}
