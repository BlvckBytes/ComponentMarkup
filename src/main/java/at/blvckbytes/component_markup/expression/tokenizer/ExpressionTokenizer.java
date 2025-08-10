/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ExpressionTokenizer {

  private @Nullable Stack<Token> pendingStack;
  private boolean isNextPendingPeek;

  private final StringView input;
  private final @Nullable TokenOutput tokenOutput;
  private boolean dashIsPrefix;

  public ExpressionTokenizer(StringView input, @Nullable TokenOutput tokenOutput) {
    this.input = input;
    this.tokenOutput = tokenOutput;

    // The only purpose of a dash at the very beginning is to flip a sign
    this.dashIsPrefix = true;
  }

  public TerminalToken parseStringToken() {
    char quoteChar;

    if ((quoteChar = input.nextChar()) != '\'' && quoteChar != '"' && quoteChar != '`')
      throw new IllegalStateException("Expected to only be called if nextChar() = '\\'' or '\"' or '`'");

    List<InterpolationMember> members = quoteChar == '`' ? new ArrayList<>() : null;

    int rawStartInclusive = input.getPosition();

    char currentChar;
    boolean isTerminated = false;

    int literalStartInclusive = rawStartInclusive + 1;

    while ((currentChar = input.nextChar()) != 0) {
      if (currentChar == '\n')
        break;

      if (quoteChar == '`') {
        if (currentChar == '{') {
          if (input.priorChar(1) == '\\') {
            int backslashPosition = input.getPosition() - 1;

            input.addIndexToBeRemoved(backslashPosition);

            if (tokenOutput != null)
              tokenOutput.emitToken(TokenType.ANY__ESCAPE_SEQUENCE, input.buildSubViewAbsolute(backslashPosition, backslashPosition + 2));

            continue;
          }

          int openingCurlyPosition = input.getPosition();

          if (openingCurlyPosition > literalStartInclusive)
            members.add(input.buildSubViewAbsolute(literalStartInclusive, openingCurlyPosition));

          ExpressionNode interpolationExpression = ExpressionParser.parseWithoutTrailingCheck(this, tokenOutput);

          input.consumeWhitespace(tokenOutput);

          if (input.nextChar() != '}')
            throw new ExpressionTokenizeException(openingCurlyPosition, ExpressionTokenizeError.UNTERMINATED_TEMPLATE_LITERAL_INTERPOLATION);

          if (interpolationExpression == null)
            throw new ExpressionTokenizeException(openingCurlyPosition, ExpressionTokenizeError.EMPTY_TEMPLATE_LITERAL_INTERPOLATION);

          members.add(interpolationExpression);

          if (tokenOutput != null) {
            StringView rawInterpolation = input.buildSubViewAbsolute(openingCurlyPosition, input.getPosition() + 1);
            tokenOutput.emitToken(TokenType.ANY__INTERPOLATION, rawInterpolation);
          }

          literalStartInclusive = input.getPosition() + 1;
          continue;
        }

        if (currentChar == '}') {
          if (input.priorChar(1) != '\\')
            throw new ExpressionTokenizeException(input.getPosition(), ExpressionTokenizeError.UNESCAPED_TEMPLATE_LITERAL_CURLY);

          int backslashPosition = input.getPosition() - 1;

          input.addIndexToBeRemoved(backslashPosition);

          if (tokenOutput != null)
            tokenOutput.emitToken(TokenType.ANY__ESCAPE_SEQUENCE, input.buildSubViewAbsolute(backslashPosition, backslashPosition + 2));
        }
      }

      if (tokenOutput != null && Character.isWhitespace(currentChar))
        tokenOutput.emitCharToken(input.getPosition(), TokenType.ANY__WHITESPACE);

      if (currentChar == quoteChar) {
        if (input.priorChar(1) != '\\') {
          isTerminated = true;
          break;
        }

        int backslashPosition = input.getPosition() - 1;

        input.addIndexToBeRemoved(backslashPosition);

        if (tokenOutput != null)
          tokenOutput.emitToken(TokenType.ANY__ESCAPE_SEQUENCE, input.buildSubViewAbsolute(backslashPosition, backslashPosition + 2));
      }
    }

    if (!isTerminated)
      throw new ExpressionTokenizeException(rawStartInclusive, ExpressionTokenizeError.UNTERMINATED_STRING);

    int rawEndInclusive = input.getPosition();

    StringView rawContents = input.buildSubViewAbsolute(rawStartInclusive, rawEndInclusive + 1);

    if (quoteChar != '`')
      return new StringToken(rawContents, rawContents.buildSubViewRelative(1, -1).buildString());

    if (rawEndInclusive > literalStartInclusive || members.isEmpty())
      members.add(input.buildSubViewAbsolute(literalStartInclusive, rawEndInclusive));

    return new TemplateLiteralToken(rawContents, members);
  }

  private Token parseIdentifierOrLiteralToken() {
    int beginInclusive = -1;
    int endInclusive = -1;

    boolean isFirst = true;
    char upcomingChar;

    while ((upcomingChar = _peekChar(0)) != 0) {
      if (Character.isWhitespace(upcomingChar))
        break;

      Token upcomingOperatorOrPunctuation;

      // The current token is most definitely an operand, so don't interpret as a prefix-operator
      dashIsPrefix = false;

      if ((upcomingOperatorOrPunctuation = tryParseOperatorOrPunctuationOrDotDoubleToken()) != null) {
        if (pendingStack == null)
          pendingStack = new Stack<>();

        pendingStack.push(upcomingOperatorOrPunctuation);
        break;
      }

      input.nextChar();

      boolean isAlphabetic = upcomingChar >= 'a' && upcomingChar <= 'z';
      boolean isNumeric = upcomingChar >= '0' && upcomingChar <= '9';

      if (isFirst) {
        beginInclusive = input.getPosition();

        if (upcomingChar == '_' || isNumeric)
          throw new ExpressionTokenizeException(beginInclusive, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

        isFirst = false;
      }

      if (!(isAlphabetic || isNumeric || upcomingChar == '_'))
        throw new ExpressionTokenizeException(beginInclusive, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      if (upcomingChar == '_' && input.priorChar(1) == '_')
        throw new ExpressionTokenizeException(beginInclusive, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      endInclusive = input.getPosition();
    }

    if (beginInclusive < 0)
      throw new IllegalStateException("Expected to only be called if peekChar(0) != 0 or ' ' or '.'");

    StringView value = input.buildSubViewAbsolute(beginInclusive, endInclusive + 1);

    switch (value.buildString()) {
      case "true":
        return new BooleanToken(value, true);

      case "false":
        return new BooleanToken(value, false);

      case "null":
        return new NullToken(value);
    }

    return new IdentifierToken(value, value.buildString());
  }

  private int collectSubsequentDigitsAndGetFirstIndex() {
    char currentChar;
    int firstIndex = -1;

    while ((currentChar = _peekChar(0)) >= '0' && currentChar <= '9') {
      input.nextChar();

      if (firstIndex < 0)
        firstIndex = input.getPosition();
    }

    return firstIndex;
  }

  public Token parseLongOrDoubleToken() {
    int startInclusive;

    if ((startInclusive = collectSubsequentDigitsAndGetFirstIndex()) < 0)
      throw new IllegalStateException("Expected to only be called if nextChar() is a digit");

    int lastDigitIndex = input.getPosition();

    if (_peekChar(0) == '.') {
      // Range-operator encountered
      if (_peekChar(1) == '.') {
        StringView value = input.buildSubViewAbsolute(startInclusive, lastDigitIndex + 1);
        return new LongToken(value, Long.parseLong(value.buildString()));
      }

      input.nextChar();

      if (collectSubsequentDigitsAndGetFirstIndex() < 0)
        throw new ExpressionTokenizeException(startInclusive, ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS);

      StringView value = input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);

      return new DoubleToken(value, Double.parseDouble(value.buildString()));
    }

    StringView value = input.buildSubViewAbsolute(startInclusive, lastDigitIndex + 1);

    return new LongToken(value, Long.parseLong(value.buildString()));
  }

  public @Nullable Token tryParseDotDoubleToken() {
    if (_peekChar(0) != '.')
      throw new IllegalStateException("Expected to only be called if peekChar(0) = '.'");

    char charAfterDot = _peekChar(1);

    if (!(charAfterDot >= '0' && charAfterDot <= '9'))
      return null;

    input.nextChar(); // .

    int startInclusive = input.getPosition();

    if (collectSubsequentDigitsAndGetFirstIndex() < 0)
      throw new IllegalStateException("Unreachable: checked for a digit's presence ahead of time");

    StringView rawValue = input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);

    return new DoubleToken(rawValue, Double.parseDouble(rawValue.buildString()));
  }

  private @Nullable Token tryParseOperatorOrPunctuationOrDotDoubleToken() {
    EnumToken enumToken;

    switch (_peekChar(0)) {
      case '(':
        enumToken = Punctuation.OPENING_PARENTHESIS;
        break;

      case ')':
        enumToken = Punctuation.CLOSING_PARENTHESIS;
        break;

      case '[':
        enumToken = InfixOperator.SUBSCRIPTING;
        break;

      case ']':
        enumToken = Punctuation.CLOSING_BRACKET;
        break;

      case ',':
        enumToken = Punctuation.COMMA;
        break;

      case '+':
        enumToken = InfixOperator.ADDITION;
        break;

      case '-':
        enumToken = dashIsPrefix ? PrefixOperator.FLIP_SIGN : InfixOperator.SUBTRACTION;
        break;

      case '*':
        if (_peekChar(1) == '*') {
          enumToken = InfixOperator.REPEAT;
          break;
        }

        enumToken = InfixOperator.MULTIPLICATION;
        break;

      case '/':
        enumToken = InfixOperator.DIVISION;
        break;

      case '%':
        enumToken = InfixOperator.MODULO;
        break;

      case '^':
        enumToken = InfixOperator.EXPONENTIATION;
        break;

      case '&':
        if (_peekChar(1) == '&') {
          enumToken = InfixOperator.CONJUNCTION;
          break;
        }

        enumToken = InfixOperator.CONCATENATION;
        break;

      case '|':
        if (_peekChar(1) == '|') {
          enumToken = InfixOperator.DISJUNCTION;
          break;
        }

        throw new ExpressionTokenizeException(input.getPosition() + 1, ExpressionTokenizeError.SINGLE_PIPE);

      case '?':
        if (_peekChar(1) == '?') {
          enumToken = InfixOperator.FALLBACK;
          break;
        }

        enumToken = InfixOperator.BRANCHING;
        break;

      case '@':
        if (_peekChar(1) == '@') {
          enumToken = InfixOperator.EXPLODE_REGEX;
          break;
        }

        enumToken = InfixOperator.EXPLODE;
        break;

      case ':':
        if (_peekChar(1) == ':') {
          if (_peekChar(2) == ':') {
            enumToken = InfixOperator.MATCHES_REGEX;
            break;
          }

          enumToken = InfixOperator.CONTAINS;
          break;
        }

        enumToken = Punctuation.COLON;
        break;

      case '>':
        if (_peekChar(1) == '=') {
          enumToken = InfixOperator.GREATER_THAN_OR_EQUAL;
          break;
        }

        enumToken = InfixOperator.GREATER_THAN;
        break;

      case '<':
        if (_peekChar(1) == '=') {
          enumToken = InfixOperator.LESS_THAN_OR_EQUAL;
          break;
        }

        enumToken = InfixOperator.LESS_THAN;
        break;

      case '=':
        if (_peekChar(1) == '=') {
          enumToken = InfixOperator.EQUAL_TO;
          break;
        }

        throw new ExpressionTokenizeException(input.getPosition() + 1, ExpressionTokenizeError.SINGLE_EQUALS);

      case '!':
        if (_peekChar(1) == '=') {
          enumToken = InfixOperator.NOT_EQUAL_TO;
          break;
        }

        enumToken = PrefixOperator.NEGATION;
        break;

      case '.':
        char upcomingChar = _peekChar(1);

        if (upcomingChar == '.') {
          enumToken = InfixOperator.RANGE;
          break;
        }

        if (upcomingChar >= '0' && upcomingChar <= '9')
          return tryParseDotDoubleToken();

        enumToken = InfixOperator.MEMBER;
        break;

      default:
        return null;
    }

    input.nextChar();

    int startInclusive = input.getPosition();

    for (int i = 1; i < enumToken.getLength(); ++i)
      input.nextChar();

    return enumToken.create(input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1));
  }

  public <T extends Token> @Nullable T peekToken(Class<T> requiredType) {
    Token token = peekToken();

    if (requiredType.isInstance(token))
      return requiredType.cast(token);

    return null;
  }

  public <T extends Token> @Nullable T nextToken(Class<T> requiredType) {
    if (requiredType.isInstance(peekToken()))
      return requiredType.cast(nextToken());

    return null;
  }

  public void putBackToken(Token token) {
    if (pendingStack == null)
      pendingStack = new Stack<>();

    pendingStack.push(token);
  }

  public @Nullable Token nextToken() {
    Token result;
    boolean wasPeek = false;

    if (pendingStack != null && !pendingStack.isEmpty()) {
      result = pendingStack.pop();
      if (isNextPendingPeek)
        wasPeek = true;

      isNextPendingPeek = false;
    }

    else {
      input.consumeWhitespace(tokenOutput);

      char upcomingChar = _peekChar(0);

      if (upcomingChar == 0)
        return null;

      if (upcomingChar == '\'' || upcomingChar == '"' || upcomingChar == '`')
        result = parseStringToken();

      else if (upcomingChar >= '0' && upcomingChar <= '9')
        result = parseLongOrDoubleToken();

      else {
        if ((upcomingChar != '.' || (result = tryParseDotDoubleToken()) == null) && (result = tryParseOperatorOrPunctuationOrDotDoubleToken()) == null)
          result = parseIdentifierOrLiteralToken();
      }
    }

    dashIsPrefix = result instanceof PrefixOperatorToken || result instanceof InfixOperatorToken || result instanceof PunctuationToken;

    if (!wasPeek)
      emitTokenToOutput(result);

    return result;
  }

  public @Nullable Token peekToken() {
    if (pendingStack == null)
      pendingStack = new Stack<>();

    if (pendingStack.isEmpty()) {
      Token token = nextToken();

      if (token != null) {
        pendingStack.push(token);
        isNextPendingPeek = true;
      }
    }

    return this.pendingStack.isEmpty() ? null : this.pendingStack.peek();
  }

  private void emitTokenToOutput(Token token) {
    if (tokenOutput == null)
      return;

    tokenOutput.emitToken(token.getType(), token.raw);
  }

  private char _peekChar(int offset) {
    char peekedChar = input.peekChar(offset);

    // Let's interpret the interpolation-delimiters as EOF, as
    // to enable parsing interpolations within strings.
    if (peekedChar == '{' || peekedChar == '}')
      return 0;

    return peekedChar;
  }
}
