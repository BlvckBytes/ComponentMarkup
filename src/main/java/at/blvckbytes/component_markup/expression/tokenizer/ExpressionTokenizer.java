/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.expression.parser.ExpressionParserError;
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

  public final InputView input;
  public final @Nullable TokenOutput tokenOutput;
  private boolean dashIsPrefix;

  public boolean allowPeekingClosingCurly;

  public ExpressionTokenizer(InputView input, @Nullable TokenOutput tokenOutput) {
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

    boolean isMultiline = false;

    if (quoteChar != '`' && input.peekChar(0) == quoteChar && input.peekChar(1) == quoteChar) {
      isMultiline = true;
      input.nextChar();
      input.nextChar();
    }

    char currentChar;
    boolean isTerminated = false;

    int literalStartInclusive = rawStartInclusive + 1;

    while ((currentChar = input.nextChar()) != 0) {
      if (currentChar == '\n' && !isMultiline)
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

          ExpressionNode interpolationExpression;

          try {
            interpolationExpression = ExpressionParser.parse(this);
          } catch (ExpressionTokenizeException expressionTokenizeException) {
            char errorChar = input.contents.charAt(expressionTokenizeException.position);

            // Don't interpret the backtick as yet another beginning of a string, but rather as the end to the template-literal
            // we're currently in, from which follows that the current interpolation has not been terminated. This may be
            // a bit hackish, but it should certainly provide better feedback to the user.
            if (expressionTokenizeException.error == ExpressionTokenizeError.UNTERMINATED_STRING && errorChar == '`')
              throw new ExpressionTokenizeException(openingCurlyPosition, ExpressionTokenizeError.UNTERMINATED_TEMPLATE_LITERAL_INTERPOLATION);

            throw expressionTokenizeException;
          } catch (ExpressionParseException expressionParseException) {
            if (expressionParseException.error == ExpressionParserError.EXPECTED_EOS)
              throw new ExpressionTokenizeException(expressionParseException.position, ExpressionTokenizeError.TRAILING_TEMPLATE_LITERAL_INTERPOLATION_TOKEN);

            throw expressionParseException;
          }

          input.consumeWhitespace(tokenOutput);

          if (input.nextChar() != '}')
            throw new ExpressionTokenizeException(openingCurlyPosition, ExpressionTokenizeError.UNTERMINATED_TEMPLATE_LITERAL_INTERPOLATION);

          if (interpolationExpression == null)
            throw new ExpressionTokenizeException(openingCurlyPosition, ExpressionTokenizeError.EMPTY_TEMPLATE_LITERAL_INTERPOLATION);

          members.add(interpolationExpression);

          if (tokenOutput != null) {
            InputView rawInterpolation = input.buildSubViewAbsolute(openingCurlyPosition, input.getPosition() + 1);
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

      if (currentChar != quoteChar)
        continue;

      if (input.priorChar(1) == '\\') {
        int backslashPosition = input.getPosition() - 1;

        input.addIndexToBeRemoved(backslashPosition);

        if (tokenOutput != null)
          tokenOutput.emitToken(TokenType.ANY__ESCAPE_SEQUENCE, input.buildSubViewAbsolute(backslashPosition, backslashPosition + 2));

        continue;
      }

      if (isMultiline) {
        if (input.peekChar(0) != quoteChar || input.peekChar(1) != quoteChar)
          continue;

        input.nextChar();
        input.nextChar();
      }

      isTerminated = true;
      break;
    }

    if (!isTerminated)
      throw new ExpressionTokenizeException(rawStartInclusive, ExpressionTokenizeError.UNTERMINATED_STRING, String.valueOf(quoteChar));

    int rawEndInclusive = input.getPosition();

    InputView rawContents = input.buildSubViewAbsolute(rawStartInclusive, rawEndInclusive + 1);

    if (quoteChar != '`') {
      InputView value = rawContents.buildSubViewRelative(
        isMultiline ? 3 : 1,
        isMultiline ? -3 : -1
      );

      return new StringToken(rawContents, value);
    }

    if (rawEndInclusive > literalStartInclusive || members.isEmpty())
      members.add(input.buildSubViewAbsolute(literalStartInclusive, rawEndInclusive));

    return new TemplateLiteralToken(rawContents, members);
  }

  private Token parseIdentifierOrLiteralToken() {
    int beginInclusive = -1;
    int endInclusive = -1;

    boolean isFirst = true;
    char upcomingChar;

    int pushedPendingIndex = -1;

    while ((upcomingChar = _peekChar(0)) != 0) {
      if (Character.isWhitespace(upcomingChar))
        break;

      Token upcomingOperatorOrPunctuation;

      // The current token is possibly an operand, so don't interpret as a prefix-operator
      dashIsPrefix = false;

      if ((upcomingOperatorOrPunctuation = tryParseOperatorOrPunctuationOrDotDoubleToken()) != null) {
        if (pendingStack == null)
          pendingStack = new Stack<>();

        pushedPendingIndex = pendingStack.size();
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

    InputView raw = input.buildSubViewAbsolute(beginInclusive, endInclusive + 1);
    String value = raw.buildString();

    switch (value) {
      case "true":
        return new BooleanToken(raw, true);

      case "false":
        return new BooleanToken(raw, false);

      case "null":
        return new NullToken(raw);
    }

    InfixOperator infixOperator = InfixOperator.byName(value);

    if (infixOperator != null)
      return new InfixOperatorToken(raw, infixOperator);

    PrefixOperator prefixOperator = PrefixOperator.byName(value);

    if (prefixOperator != null && !prefixOperator.flags.contains(OperatorFlag.PARENS)) {
      // Did push a token which was wedged against the identifier that terminated it
      if (pushedPendingIndex >= 0) {
        Token pushedToken = pendingStack.get(pushedPendingIndex);

        // Was an infix-operator that could've possibly been affected by wrong dash-interpretation
        if (pushedToken instanceof InfixOperatorToken) {
          if (((InfixOperatorToken) pushedToken).operator == InfixOperator.SUBTRACTION)
            pendingStack.set(pushedPendingIndex, new PrefixOperatorToken(pushedToken.raw, PrefixOperator.FLIP_SIGN));
        }
      }

      return new PrefixOperatorToken(raw, prefixOperator);
    }

    return new IdentifierToken(raw, raw.buildString());
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
        InputView value = input.buildSubViewAbsolute(startInclusive, lastDigitIndex + 1);
        return new LongToken(value, Long.parseLong(value.buildString()));
      }

      input.nextChar();

      if (collectSubsequentDigitsAndGetFirstIndex() < 0)
        throw new ExpressionTokenizeException(startInclusive, ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS);

      InputView value = input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);

      return new DoubleToken(value, Double.parseDouble(value.buildString()));
    }

    InputView value = input.buildSubViewAbsolute(startInclusive, lastDigitIndex + 1);

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

    InputView rawValue = input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);

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

      case '{':
        enumToken = Punctuation.OPENING_CURLY;
        break;

      case '}':
        enumToken = Punctuation.CLOSING_CURLY;
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
        enumToken = InfixOperator.CONCATENATION;
        break;

      case '?':
        if (_peekChar(1) == '?') {
          enumToken = InfixOperator.FALLBACK;
          break;
        }

        enumToken = InfixOperator.BRANCHING_THEN;
        break;

      case ':':
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

    // Otherwise, the tokenizer would consume possible interpolation-terminators.
    // The expression-parser calling into the tokenizer may toggle this flag whenever it
    // expects a closing-curly, as it keeps track of balance on the call-stack.
    if (!allowPeekingClosingCurly && peekedChar == '}')
      return 0;

    return peekedChar;
  }
}
