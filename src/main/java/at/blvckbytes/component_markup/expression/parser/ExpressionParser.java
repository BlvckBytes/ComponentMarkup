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
    ExpressionNode lhs = parsePrefixOperations();

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

      throw new ExpressionParserException(ExpressionParserError.EXPECTED_RHS_OPERAND, upcomingToken);
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
      Token delimiterToken;

      if ((delimiterToken = tokenizer.nextToken()) instanceof PunctuationToken) {
        PunctuationToken punctuationToken = (PunctuationToken) delimiterToken;

        if (punctuationToken.punctuation == Punctuation.COLON) {
          ExpressionNode falseBranch = parseExpression(null);

          if (falseBranch == null)
            throw new IllegalStateException("Expected false-branch of ? after :, but got nothing");

          return new IfElseNode(lhs, operatorToken, rhs, punctuationToken, falseBranch);
        }
      }

      throw new IllegalStateException("Expected : delimiter for ? branching, but got " + (delimiterToken == null ? "nothing" : delimiterToken.getClass().getSimpleName()));
    }

    return new InfixOperationNode(lhs, operatorToken, rhs);
  }

  private @Nullable ExpressionNode parsePrefixOperations() {
    Token operatorToken;

    if (!((operatorToken = tokenizer.peekToken()) instanceof PrefixOperatorToken))
      return parseArrayExpression();

    PrefixOperatorToken prefixToken = (PrefixOperatorToken) operatorToken;

    tokenizer.nextToken();

    ExpressionNode operand = parseExpression(null);

    if (operand == null)
      throw new IllegalStateException("Expected an operand for " + prefixToken.operator + " but got nothing");

    return new PrefixOperationNode(prefixToken, operand);
  }

  private @Nullable ExpressionNode parseArrayExpression() {
    Token introductionToken;

    if (!((introductionToken = tokenizer.peekToken()) instanceof InfixOperatorToken))
      return parseParenthesesExpression();

    InfixOperatorToken introductionOperator = (InfixOperatorToken) introductionToken;

    if (introductionOperator.operator != InfixOperator.SUBSCRIPTING)
      return parseParenthesesExpression();

    tokenizer.nextToken();

    List<ExpressionNode> arrayItems = new ArrayList<>();

    while (tokenizer.peekToken() != null) {
      ExpressionNode arrayItem = parseExpression(null);

      if (arrayItem == null) {
        if (arrayItems.isEmpty())
          break;

        throw new IllegalStateException("Expected an array-item after a trailing comma");
      }

      arrayItems.add(arrayItem);

      Token delimiterToken;

      if ((delimiterToken = tokenizer.peekToken()) instanceof PunctuationToken) {
        PunctuationToken delimiterPunctuation = (PunctuationToken) delimiterToken;

        if (delimiterPunctuation.punctuation == Punctuation.CLOSING_BRACKET)
          break;

        if (delimiterPunctuation.punctuation == Punctuation.COMMA) {
          tokenizer.nextToken();
          continue;
        }
      }

      throw new IllegalStateException("Expected , to separate array-items or ] to terminate array, but got " + (delimiterToken == null ? "nothing" : delimiterToken.getClass().getSimpleName()));
    }

    Token terminatorToken;

    if ((terminatorToken = tokenizer.nextToken()) instanceof PunctuationToken) {
      PunctuationToken terminatorPunctuation = (PunctuationToken) terminatorToken;

      if (terminatorPunctuation.punctuation == Punctuation.CLOSING_BRACKET)
        return new ArrayNode(introductionOperator, arrayItems, terminatorPunctuation);
    }

    throw new IllegalStateException("Expected closing ] of array");
  }

  private @Nullable ExpressionNode parseParenthesesExpression() {
    Token introductionToken;

    if (!((introductionToken = tokenizer.peekToken()) instanceof PunctuationToken))
      return parseTerminalNode();

    PunctuationToken introductionPunctuation = (PunctuationToken) introductionToken;

    if (introductionPunctuation.punctuation != Punctuation.OPENING_PARENTHESIS)
      return parseTerminalNode();

    tokenizer.nextToken();

    ExpressionNode expression = parseExpression(null);

    if (expression == null)
      throw new IllegalStateException("Expected contents of parentheses, but got nothing");

    Token terminationToken;

    if ((terminationToken = tokenizer.nextToken()) instanceof PunctuationToken) {
      PunctuationToken terminationPunctuation = (PunctuationToken) terminationToken;

      if (terminationPunctuation.punctuation == Punctuation.CLOSING_PARENTHESIS)
        return expression;
    }

    throw new IllegalStateException("Expecting closing-parentheses, but got " + (terminationToken == null ? "nothing" : terminationToken.getClass().getSimpleName()));
  }

  private @Nullable ExpressionNode parseTerminalNode() {
    Token token = tokenizer.peekToken();

    if (!(token instanceof TerminalToken))
      return null;

    tokenizer.nextToken();
    return new TerminalNode((TerminalToken) token);
  }
}
