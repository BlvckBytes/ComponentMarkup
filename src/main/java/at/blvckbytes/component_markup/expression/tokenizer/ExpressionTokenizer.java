package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class ExpressionTokenizer {

  private final Stack<Token> pendingStack;
  private boolean isNextPendingPeek;

  private final StringView input;
  private final @Nullable TokenOutput tokenOutput;
  private boolean dashIsPrefix;

  public ExpressionTokenizer(StringView input, @Nullable TokenOutput tokenOutput) {
    this.pendingStack = new Stack<>();
    this.input = input;
    this.tokenOutput = tokenOutput;

    // The only purpose of a dash at the very beginning is to flip a sign
    this.dashIsPrefix = true;
  }

  private Token parseStringToken() {
    char quoteChar;

    if ((quoteChar = input.nextChar()) != '\'' && quoteChar != '"')
      throw new IllegalStateException("Expected to only be called if nextChar() = '\\'' or '\"'");

    input.setSubViewStart(input.getPosition());

    char upcomingChar;
    boolean isTerminated = false;

    while ((upcomingChar = input.nextChar()) != 0) {
      if (upcomingChar == '\n')
        break;

      if (upcomingChar == quoteChar) {
        if (input.priorNextChar() != '\\') {
          isTerminated = true;
          break;
        }

        input.addIndexToBeRemoved(input.getCharIndex() - 1);
      }
    }

    if (!isTerminated)
      throw new ExpressionTokenizeException(input.getSubViewStart(), ExpressionTokenizeError.UNTERMINATED_STRING);

    StringView rawContents = input.buildSubViewInclusive(PositionMode.CURRENT);
    StringView stringContents = rawContents.buildSubViewRelative(1, -1);

    return new StringToken(rawContents, stringContents);
  }

  private Token parseIdentifierOrLiteralToken() {
    StringPosition begin = input.getPosition(PositionMode.NEXT);
    StringPosition end = begin;

    boolean isFirst = true;
    char upcomingChar;

    while ((upcomingChar = input.peekChar()) != 0) {
      if (Character.isWhitespace(upcomingChar))
        break;

      Token upcomingOperatorOrPunctuation;

      // The current token is most definitely an operand, so don't interpret as a prefix-operator
      dashIsPrefix = false;

      if ((upcomingOperatorOrPunctuation = tryParseOperatorOrPunctuationToken()) != null) {
        pendingStack.push(upcomingOperatorOrPunctuation);
        break;
      }

      boolean isAlphabetic = upcomingChar >= 'a' && upcomingChar <= 'z';
      boolean isNumeric = upcomingChar >= '0' && upcomingChar <= '9';

      if (!(isAlphabetic || isNumeric || upcomingChar == '_'))
        throw new ExpressionTokenizeException(begin, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      if (isFirst && (upcomingChar == '_' || isNumeric))
        throw new ExpressionTokenizeException(begin, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      if (upcomingChar == '_' && input.priorNextChar() == '_')
        throw new ExpressionTokenizeException(begin, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      input.nextChar();
      isFirst = false;
      end = input.getPosition();
    }

    input.setSubViewStart(begin);
    StringView value = input.buildSubViewInclusive(end);

    if (value.isEmpty())
      throw new IllegalStateException("Expected to only be called if peekChar() != 0 or ' ' or '.'");

    switch (value.buildString()) {
      case "true":
        return new BooleanToken(value, true);

      case "false":
        return new BooleanToken(value, false);

      case "null":
        return new NullToken(value);
    }

    return new IdentifierToken(value);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean collectSubsequentDigits() {
    char currentChar;
    boolean collectedDigits = false;

    while ((currentChar = input.peekChar()) >= '0' && currentChar <= '9') {
      collectedDigits = true;
      input.nextChar();
    }

    return collectedDigits;
  }

  private Token parseLongOrDoubleToken() {
    input.setSubViewStart(input.getPosition(PositionMode.NEXT));

    if (!collectSubsequentDigits())
      throw new IllegalStateException("Expected to only be called if nextChar() is a digit");

    StringPosition savePoint = input.getPosition();

    if (input.peekChar() == '.') {
      input.nextChar();

      if (input.peekChar() == '.') {
        input.restorePosition(savePoint);

        StringView value = input.buildSubViewInclusive(PositionMode.CURRENT);

        return new LongToken(value, value.parseLong());
      }

      if (!collectSubsequentDigits())
        throw new ExpressionTokenizeException(input.getSubViewStart(), ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS);

      StringView value = input.buildSubViewInclusive(PositionMode.CURRENT);

      return new DoubleToken(value, value.parseDouble());
    }

    StringView value = input.buildSubViewInclusive(PositionMode.CURRENT);

    return new LongToken(value, value.parseLong());
  }

  private @Nullable Token tryParseDotDoubleToken() {
    StringPosition savePoint = input.getPosition();

    if (input.nextChar() != '.')
      throw new IllegalStateException("Expected to only be called if nextChar() = '.'");

    StringPosition beginPosition = input.getPosition();

    if (!collectSubsequentDigits()) {
      input.restorePosition(savePoint);
      return null;
    }

    input.setSubViewStart(beginPosition);
    StringView rawValue = input.buildSubViewInclusive(PositionMode.CURRENT);

    return new DoubleToken(rawValue, rawValue.parseDouble());
  }

  private @Nullable Token tryParseOperatorOrPunctuationToken() {
    StringPosition savePoint = input.getPosition();

    char firstChar = input.nextChar();

    if (firstChar == 0)
      return null;

    input.setSubViewStart(input.getPosition());

    switch (firstChar) {
      case '(':
        return new PunctuationToken(input.buildSubViewInclusive(PositionMode.CURRENT), Punctuation.OPENING_PARENTHESIS);

      case ')':
        return new PunctuationToken(input.buildSubViewInclusive(PositionMode.CURRENT), Punctuation.CLOSING_PARENTHESIS);

      case '[':
        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.SUBSCRIPTING);

      case ']':
        return new PunctuationToken(input.buildSubViewInclusive(PositionMode.CURRENT), Punctuation.CLOSING_BRACKET);

      case ',':
        return new PunctuationToken(input.buildSubViewInclusive(PositionMode.CURRENT), Punctuation.COMMA);

      case '+':
        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.ADDITION);

      case '-':
        if (dashIsPrefix)
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.FLIP_SIGN);

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.SUBTRACTION);

      case '*':
        if (input.peekChar() == '*') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.REPEAT);
        }

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.MULTIPLICATION);

      case '/':
        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.DIVISION);

      case '%':
        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.MODULO);

      case '^':
        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.EXPONENTIATION);

      case '&':
        if (input.peekChar() == '&') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.CONJUNCTION);
        }

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.CONCATENATION);

      case '|':
        if (input.peekChar() == '|') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.DISJUNCTION);
        }

        throw new ExpressionTokenizeException(input.getSubViewStart(), ExpressionTokenizeError.SINGLE_PIPE);

      case '?':
        if (input.peekChar() == '?') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.FALLBACK);
        }

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.BRANCHING);

      case '@':
        if (input.peekChar() == '@') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.EXPLODE_REGEX);
        }

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.EXPLODE);

      case ':':
        return new PunctuationToken(input.buildSubViewInclusive(PositionMode.CURRENT), Punctuation.COLON);

      case '>':
        if (input.peekChar() == '=') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.GREATER_THAN_OR_EQUAL);
        }

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.GREATER_THAN);

      case '<':
        if (input.peekChar() == '=') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.LESS_THAN_OR_EQUAL);
        }

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.LESS_THAN);

      case '=':
        if (input.peekChar() == '=') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.EQUAL_TO);
        }

        throw new ExpressionTokenizeException(input.getSubViewStart(), ExpressionTokenizeError.SINGLE_EQUALS);

      case '!':
        if (input.peekChar() == '=') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.NOT_EQUAL_TO);
        }

        return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.NEGATION);

      case '~':
        if (input.peekChar() == '^') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.UPPER_CASE);
        }

        if (input.peekChar() == '_') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.LOWER_CASE);
        }

        if (input.peekChar() == '#') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.TITLE_CASE);
        }

        if (input.peekChar() == '!') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.TOGGLE_CASE);
        }

        if (input.peekChar() == '-') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.SLUGIFY);
        }

        if (input.peekChar() == '?') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.ASCIIFY);
        }

        if (input.peekChar() == '|') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.TRIM);
        }

        if (input.peekChar() == '<') {
          input.nextChar();
          return new PrefixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), PrefixOperator.REVERSE);
        }

        throw new ExpressionTokenizeException(input.getSubViewStart(), ExpressionTokenizeError.SINGLE_TILDE);

      case '.':
        char upcomingChar = input.peekChar();

        if (upcomingChar == '.') {
          input.nextChar();
          return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.RANGE);
        }

        if (upcomingChar >= '0' && upcomingChar <= '9') {
          input.restorePosition(savePoint);
          input.clearSubViewStart();

          return tryParseDotDoubleToken();
        }

        return new InfixOperatorToken(input.buildSubViewInclusive(PositionMode.CURRENT), InfixOperator.MEMBER);
    }

    input.restorePosition(savePoint);
    input.clearSubViewStart();

    return null;
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

  public @Nullable Token nextToken() {
    Token result;
    boolean wasPeek = false;

    if (!this.pendingStack.isEmpty()) {
      result = this.pendingStack.pop();
      if (isNextPendingPeek)
        wasPeek = true;

      isNextPendingPeek = false;
    }

    else {
      input.consumeWhitespaceAndGetIfNewline(tokenOutput);

      char upcomingChar = input.peekChar();

      if (upcomingChar == 0)
        return null;

      if (upcomingChar == '\'' || upcomingChar == '"')
        result = parseStringToken();

      else if (upcomingChar >= '0' && upcomingChar <= '9')
        result = parseLongOrDoubleToken();

      else {
        if ((upcomingChar != '.' || (result = tryParseDotDoubleToken()) == null) && (result = tryParseOperatorOrPunctuationToken()) == null)
          result = parseIdentifierOrLiteralToken();
      }
    }

    dashIsPrefix = result instanceof PrefixOperatorToken || result instanceof InfixOperatorToken || result instanceof PunctuationToken;

    if (!wasPeek)
      emitTokenToOutput(result);

    return result;
  }

  public @Nullable Token peekToken() {
    if (this.pendingStack.isEmpty()) {
      this.pendingStack.push(nextToken());
      isNextPendingPeek = true;
    }

    return this.pendingStack.peek();
  }

  private void emitTokenToOutput(Token token) {
    if (tokenOutput == null)
      return;

    tokenOutput.emitToken(token.getType(), token.raw);
  }
}
