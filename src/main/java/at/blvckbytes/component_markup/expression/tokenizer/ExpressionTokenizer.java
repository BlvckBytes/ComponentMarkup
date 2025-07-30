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

    int startInclusive = input.getPosition();

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

        input.addIndexToBeRemoved(input.getPosition() - 1);
      }
    }

    if (!isTerminated)
      throw new ExpressionTokenizeException(startInclusive, ExpressionTokenizeError.UNTERMINATED_STRING);

    int endInclusive = input.getPosition();

    StringView rawContents = input.buildSubViewAbsolute(startInclusive, endInclusive + 1);
    StringView stringContents = input.buildSubViewAbsolute(startInclusive + 1, endInclusive);

    return new StringToken(rawContents, stringContents);
  }

  private Token parseIdentifierOrLiteralToken() {
    int beginInclusive = -1;
    int endInclusive = -1;

    boolean isFirst = true;
    char upcomingChar;

    while ((upcomingChar = input.peekChar(0)) != 0) {
      if (Character.isWhitespace(upcomingChar))
        break;

      Token upcomingOperatorOrPunctuation;

      // The current token is most definitely an operand, so don't interpret as a prefix-operator
      dashIsPrefix = false;

      if ((upcomingOperatorOrPunctuation = tryParseOperatorOrPunctuationOrDotDoubleToken()) != null) {
        pendingStack.push(upcomingOperatorOrPunctuation);
        break;
      }

      input.nextChar();

      char priorChar = input.priorNextChar();

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

      if (upcomingChar == '_' && priorChar == '_')
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

    while ((currentChar = input.peekChar(0)) >= '0' && currentChar <= '9') {
      input.nextChar();

      if (firstIndex < 0)
        firstIndex = input.getPosition();
    }

    return firstIndex;
  }

  private Token parseLongOrDoubleToken() {
    int startInclusive;

    if ((startInclusive = collectSubsequentDigitsAndGetFirstIndex()) < 0)
      throw new IllegalStateException("Expected to only be called if nextChar() is a digit");

    int lastDigitIndex = input.getPosition();

    if (input.peekChar(0) == '.') {
      // Range-operator encountered
      if (input.peekChar(1) == '.') {
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

  private @Nullable Token tryParseDotDoubleToken() {
    if (input.peekChar(0) != '.')
      throw new IllegalStateException("Expected to only be called if peekChar(0) = '.'");

    char charAfterDot = input.peekChar(1);

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

    switch (input.peekChar(0)) {
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
        if (input.peekChar(1) == '*') {
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
        if (input.peekChar(1) == '&') {
          enumToken = InfixOperator.CONJUNCTION;
          break;
        }

        enumToken = InfixOperator.CONCATENATION;
        break;

      case '|':
        if (input.peekChar(1) == '|') {
          enumToken = InfixOperator.DISJUNCTION;
          break;
        }

        throw new ExpressionTokenizeException(input.getPosition() + 1, ExpressionTokenizeError.SINGLE_PIPE);

      case '?':
        if (input.peekChar(1) == '?') {
          enumToken = InfixOperator.FALLBACK;
          break;
        }

        enumToken = InfixOperator.BRANCHING;
        break;

      case '@':
        if (input.peekChar(1) == '@') {
          enumToken = InfixOperator.EXPLODE_REGEX;
          break;
        }

        enumToken = InfixOperator.EXPLODE;
        break;

      case ':':
        if (input.peekChar(1) == ':') {
          if (input.peekChar(2) == ':') {
            enumToken = InfixOperator.MATCHES_REGEX;
            break;
          }

          enumToken = InfixOperator.CONTAINS;
          break;
        }

        enumToken = Punctuation.COLON;
        break;

      case '>':
        if (input.peekChar(1) == '=') {
          enumToken = InfixOperator.GREATER_THAN_OR_EQUAL;
          break;
        }

        enumToken = InfixOperator.GREATER_THAN;
        break;

      case '<':
        if (input.peekChar(1) == '=') {
          enumToken = InfixOperator.LESS_THAN_OR_EQUAL;
          break;
        }

        enumToken = InfixOperator.LESS_THAN;
        break;

      case '=':
        if (input.peekChar(1) == '=') {
          enumToken = InfixOperator.EQUAL_TO;
          break;
        }

        throw new ExpressionTokenizeException(input.getPosition() + 1, ExpressionTokenizeError.SINGLE_EQUALS);

      case '!':
        if (input.peekChar(1) == '=') {
          enumToken = InfixOperator.NOT_EQUAL_TO;
          break;
        }

        enumToken = PrefixOperator.NEGATION;
        break;

      case '~':
        switch (input.peekChar(1)) {
          case '^':
            enumToken = PrefixOperator.UPPER_CASE;
            break;

          case '_':
            enumToken = PrefixOperator.LOWER_CASE;
            break;

          case '#':
            enumToken = PrefixOperator.TITLE_CASE;
            break;

          case '!':
            enumToken = PrefixOperator.TOGGLE_CASE;
            break;

          case '-':
            enumToken = PrefixOperator.SLUGIFY;
            break;

          case '?':
            enumToken = PrefixOperator.ASCIIFY;
            break;

          case '|':
            enumToken = PrefixOperator.TRIM;
            break;

          case '<':
            enumToken = PrefixOperator.REVERSE;
            break;

          default:
            throw new ExpressionTokenizeException(input.getPosition() + 1, ExpressionTokenizeError.SINGLE_TILDE);
        }
        break;

      case '.':
        char upcomingChar = input.peekChar(1);

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

      char upcomingChar = input.peekChar(0);

      if (upcomingChar == 0)
        return null;

      if (upcomingChar == '\'' || upcomingChar == '"')
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
