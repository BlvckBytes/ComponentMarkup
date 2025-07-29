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

    while ((upcomingChar = input.peekChar()) != 0) {
      if (Character.isWhitespace(upcomingChar))
        break;

      Token upcomingOperatorOrPunctuation;

      // The current token is most definitely an operand, so don't interpret as a prefix-operator
      dashIsPrefix = false;

      if ((upcomingOperatorOrPunctuation = tryParseOperatorOrPunctuationOrDotDoubleToken()) != null) {
        pendingStack.push(upcomingOperatorOrPunctuation);
        break;
      }

      char priorChar = input.priorNextChar();

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

      if (upcomingChar == '_' && priorChar == '_')
        throw new ExpressionTokenizeException(beginInclusive, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      endInclusive = input.getPosition();
    }

    if (beginInclusive < 0)
      throw new IllegalStateException("Expected to only be called if peekChar() != 0 or ' ' or '.'");

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

    while ((currentChar = input.peekChar()) >= '0' && currentChar <= '9') {
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

    int savePoint = input.getPosition();

    if (input.peekChar() == '.') {
      input.nextChar();

      if (input.peekChar() == '.') {
        input.restorePosition(savePoint);

        StringView value = input.buildSubViewAbsolute(startInclusive, savePoint + 1);

        return new LongToken(value, Long.parseLong(value.buildString()));
      }

      if (collectSubsequentDigitsAndGetFirstIndex() < 0)
        throw new ExpressionTokenizeException(startInclusive, ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS);

      StringView value = input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);

      return new DoubleToken(value, Double.parseDouble(value.buildString()));
    }

    StringView value = input.buildSubViewAbsolute(startInclusive, savePoint + 1);

    return new LongToken(value, Long.parseLong(value.buildString()));
  }

  private @Nullable Token tryParseDotDoubleToken() {
    int savePoint = input.getPosition();

    if (input.nextChar() != '.')
      throw new IllegalStateException("Expected to only be called if nextChar() = '.'");

    int startInclusive = input.getPosition();

    if (collectSubsequentDigitsAndGetFirstIndex() < 0) {
      input.restorePosition(savePoint);
      return null;
    }

    StringView rawValue = input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);

    return new DoubleToken(rawValue, Double.parseDouble(rawValue.buildString()));
  }

  private @Nullable Token tryParseOperatorOrPunctuationOrDotDoubleToken() {
    int savePoint = input.getPosition();

    char firstChar = input.nextChar();
    int startInclusive = input.getPosition();

    EnumToken enumToken;

    switch (firstChar) {
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
        if (input.peekChar() == '*') {
          input.nextChar();
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
        if (input.peekChar() == '&') {
          input.nextChar();
          enumToken = InfixOperator.CONJUNCTION;
          break;
        }

        enumToken = InfixOperator.CONCATENATION;
        break;

      case '|':
        if (input.peekChar() == '|') {
          input.nextChar();
          enumToken = InfixOperator.DISJUNCTION;
          break;
        }

        throw new ExpressionTokenizeException(startInclusive, ExpressionTokenizeError.SINGLE_PIPE);

      case '?':
        if (input.peekChar() == '?') {
          input.nextChar();
          enumToken = InfixOperator.FALLBACK;
          break;
        }

        enumToken = InfixOperator.BRANCHING;
        break;

      case '@':
        if (input.peekChar() == '@') {
          input.nextChar();
          enumToken = InfixOperator.EXPLODE_REGEX;
          break;
        }

        enumToken = InfixOperator.EXPLODE;
        break;

      case ':':
        if (input.peekChar() == ':') {
          input.nextChar();
          if (input.peekChar() == ':') {
            input.nextChar();
            enumToken = InfixOperator.MATCHES_REGEX;
            break;
          }
          enumToken = InfixOperator.CONTAINS;
          break;
        }
        enumToken = Punctuation.COLON;
        break;

      case '>':
        if (input.peekChar() == '=') {
          input.nextChar();
          enumToken = InfixOperator.GREATER_THAN_OR_EQUAL;
          break;
        }

        enumToken = InfixOperator.GREATER_THAN;
        break;

      case '<':
        if (input.peekChar() == '=') {
          input.nextChar();
          enumToken = InfixOperator.LESS_THAN_OR_EQUAL;
          break;
        }

        enumToken = InfixOperator.LESS_THAN;
        break;

      case '=':
        if (input.peekChar() == '=') {
          input.nextChar();
          enumToken = InfixOperator.EQUAL_TO;
          break;
        }

        throw new ExpressionTokenizeException(startInclusive, ExpressionTokenizeError.SINGLE_EQUALS);

      case '!':
        if (input.peekChar() == '=') {
          input.nextChar();
          enumToken = InfixOperator.NOT_EQUAL_TO;
          break;
        }

        enumToken = PrefixOperator.NEGATION;
        break;

      case '~':
        if (input.peekChar() == '^') {
          input.nextChar();
          enumToken = PrefixOperator.UPPER_CASE;
          break;
        }

        if (input.peekChar() == '_') {
          input.nextChar();
          enumToken = PrefixOperator.LOWER_CASE;
          break;
        }

        if (input.peekChar() == '#') {
          input.nextChar();
          enumToken = PrefixOperator.TITLE_CASE;
          break;
        }

        if (input.peekChar() == '!') {
          input.nextChar();
          enumToken = PrefixOperator.TOGGLE_CASE;
          break;
        }

        if (input.peekChar() == '-') {
          input.nextChar();
          enumToken = PrefixOperator.SLUGIFY;
          break;
        }

        if (input.peekChar() == '?') {
          input.nextChar();
          enumToken = PrefixOperator.ASCIIFY;
          break;
        }

        if (input.peekChar() == '|') {
          input.nextChar();
          enumToken = PrefixOperator.TRIM;
          break;
        }

        if (input.peekChar() == '<') {
          input.nextChar();
          enumToken = PrefixOperator.REVERSE;
          break;
        }

        throw new ExpressionTokenizeException(startInclusive, ExpressionTokenizeError.SINGLE_TILDE);

      case '.':
        char upcomingChar = input.peekChar();

        if (upcomingChar == '.') {
          input.nextChar();
          enumToken = InfixOperator.RANGE;
          break;
        }

        if (upcomingChar >= '0' && upcomingChar <= '9') {
          input.restorePosition(savePoint);
          return tryParseDotDoubleToken();
        }

        enumToken = InfixOperator.MEMBER;
        break;

      default:
        input.restorePosition(savePoint);
        return null;
    }

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

      char upcomingChar = input.peekChar();

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
