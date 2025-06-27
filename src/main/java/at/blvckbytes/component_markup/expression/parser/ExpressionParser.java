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
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_EOS, trailingToken.beginIndex);

    return result;
  }

  private @Nullable ExpressionNode parseExpression(@Nullable InfixOperator priorOperator) {
    ExpressionNode lhs = parseArrayExpression();

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

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_RIGHT_INFIX_OPERAND, upcomingToken.endIndex);
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
        throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND, terminationToken.beginIndex);

      return new SubstringNode(operand, operatorToken, lowerBound, colonToken, null, terminationToken);
    }

    ExpressionNode upperBound = parseExpression(null);

    if (upperBound == null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSTRING_UPPER_BOUND, colonToken.endIndex);

    terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken == null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET, upperBound.getEndIndex());

    if (terminationToken.punctuation != Punctuation.CLOSING_BRACKET)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSTRING_CLOSING_BRACKET, terminationToken.beginIndex);

    return new SubstringNode(operand, operatorToken, lowerBound, colonToken, upperBound, terminationToken);
  }

  private ExpressionNode makeInfixExpression(ExpressionNode lhs, InfixOperatorToken operatorToken, ExpressionNode rhs) {
    if (operatorToken.operator == InfixOperator.SUBSCRIPTING) {
      PunctuationToken delimiterToken = tokenizer.nextToken(PunctuationToken.class);

      if (delimiterToken == null)
        throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET, rhs.getEndIndex());

      if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET)
        return new InfixOperationNode(lhs, operatorToken, rhs, delimiterToken);

      if (delimiterToken.punctuation == Punctuation.COLON)
        return parseSubstringExpression(lhs, operatorToken, rhs, delimiterToken);

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_SUBSCRIPT_CLOSING_BRACKET, delimiterToken.beginIndex);
    }

    if (operatorToken.operator == InfixOperator.BRANCHING) {
      PunctuationToken delimiterToken = tokenizer.nextToken(PunctuationToken.class);

      if (delimiterToken == null)
        throw new ExpressionParserException(ExpressionParserError.EXPECTED_BRANCH_DELIMITER, rhs.getEndIndex());

      if (delimiterToken.punctuation != Punctuation.COLON)
        throw new ExpressionParserException(ExpressionParserError.EXPECTED_BRANCH_DELIMITER, delimiterToken.beginIndex);

      ExpressionNode falseBranch = parseExpression(null);

      if (falseBranch == null)
        throw new ExpressionParserException(ExpressionParserError.EXPECTED_FALSE_BRANCH, delimiterToken.endIndex);

      return new IfElseNode(lhs, operatorToken, rhs, delimiterToken, falseBranch);
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

    tokenizer.nextToken();

    List<ExpressionNode> arrayItems = new ArrayList<>();

    PunctuationToken delimiterToken = null;

    while (true) {
      ExpressionNode arrayItem = parseExpression(null);

      if (arrayItem == null) {
        if (arrayItems.isEmpty())
          break;

        throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_ITEM, delimiterToken.endIndex);
      }

      arrayItems.add(arrayItem);

      delimiterToken = tokenizer.peekToken(PunctuationToken.class);

      if (delimiterToken == null)
        throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET, arrayItem.getEndIndex());

      if (delimiterToken.punctuation == Punctuation.CLOSING_BRACKET)
        break;

      if (delimiterToken.punctuation == Punctuation.COMMA) {
        tokenizer.nextToken();
        continue;
      }

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET, delimiterToken.beginIndex);
    }

    PunctuationToken terminatorToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminatorToken == null) {
      int index;

      if (!arrayItems.isEmpty())
        index = arrayItems.get(arrayItems.size() - 1).getEndIndex();
      else
        index = introductionToken.endIndex;

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET, index);
    }

    if (terminatorToken.punctuation != Punctuation.CLOSING_BRACKET)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_ARRAY_CLOSING_BRACKET, terminatorToken.beginIndex);

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

    ExpressionNode operand = parseParenthesesExpression();

    if (operand == null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_PREFIX_OPERAND, operatorToken.endIndex);

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
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_PARENTHESES_CONTENT, introductionToken.endIndex);

    PunctuationToken terminationToken = tokenizer.nextToken(PunctuationToken.class);

    if (terminationToken == null)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS, expression.getEndIndex());

    if (terminationToken.punctuation != Punctuation.CLOSING_PARENTHESIS)
      throw new ExpressionParserException(ExpressionParserError.EXPECTED_CLOSING_PARENTHESIS, terminationToken.beginIndex);

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
