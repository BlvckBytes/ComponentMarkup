package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class ExpressionTokenizer {

  private final Stack<Token> pendingStack;
  private final String input;
  private int nextCharIndex;
  private boolean dashIsPrefix;

  public ExpressionTokenizer(String input) {
    this.pendingStack = new Stack<>();
    this.input = input;
    this.nextCharIndex = 0;

    // The only purpose of a dash at the very beginning is to flip a sign
    this.dashIsPrefix = true;
  }

  private char nextChar() {
    if (this.nextCharIndex == input.length())
      return 0;

    return input.charAt(this.nextCharIndex++);
  }

  private char peekChar() {
    if (this.nextCharIndex == input.length())
      return 0;

    return input.charAt(this.nextCharIndex);
  }

  private Token parseStringToken() {
    int beginIndex = nextCharIndex;

    char priorChar;

    if ((priorChar = nextChar()) != '\'')
      throw new IllegalStateException("Expected to only be called if nextChar() = '\\''");

    char upcomingChar;
    boolean isTerminated = false;

    while ((upcomingChar = nextChar()) != 0) {
      if (upcomingChar == '\n' || (upcomingChar == '\'' && priorChar != '\\')) {
        isTerminated = upcomingChar == '\'';
        break;
      }

      priorChar = upcomingChar;
    }

    if (!isTerminated)
      throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.UNTERMINATED_STRING);

    String contents = input.substring(beginIndex + 1, nextCharIndex - 1);

    return new StringToken(beginIndex, contents);
  }

  private Token parseIdentifierOrLiteralToken() {
    int beginIndex = nextCharIndex;
    int endIndexExclusive = beginIndex;

    char upcomingChar;
    char priorChar = 0;

    while ((upcomingChar = peekChar()) != 0) {
      if (Character.isWhitespace(upcomingChar))
        break;

      Token upcomingOperatorOrPunctuation;

      // The current token is most definitely an operand, so don't interpret as a prefix-operator
      dashIsPrefix = false;

      if ((upcomingOperatorOrPunctuation = tryParseOperatorOrPunctuationToken()) != null) {
        pendingStack.push(upcomingOperatorOrPunctuation);
        break;
      }

      boolean isFirst = beginIndex == nextCharIndex;
      boolean isAlphabetic = upcomingChar >= 'a' && upcomingChar <= 'z';
      boolean isNumeric = upcomingChar >= '0' && upcomingChar <= '9';

      if (!(isAlphabetic || isNumeric || upcomingChar == '_'))
        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      if (isFirst && (upcomingChar == '_' || isNumeric))
        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      if (upcomingChar == '_' && priorChar == '_')
        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.MALFORMED_IDENTIFIER);

      priorChar = nextChar();
      endIndexExclusive = nextCharIndex;
    }

    if (beginIndex == nextCharIndex)
      throw new IllegalStateException("Expected to only be called if peekChar() != 0 or ' ' or '.'");

    String value = input.substring(beginIndex, endIndexExclusive);

    switch (value) {
      case "true":
        return new BooleanToken(beginIndex, "true", true);

      case "false":
        return new BooleanToken(beginIndex, "false", false);

      case "null":
        return new NullToken(beginIndex, "null");
    }

    return new IdentifierToken(beginIndex, value);
  }

  private boolean collectSubsequentDigits() {
    int beginIndex = nextCharIndex;

    char currentChar;

    while ((currentChar = peekChar()) >= '0' && currentChar <= '9')
      nextChar();

    return beginIndex != nextCharIndex;
  }

  private Token parseLongOrDoubleToken() {
    int beginIndex = nextCharIndex;

    if (!collectSubsequentDigits())
      throw new IllegalStateException("Expected to only be called if nextChar() is a digit");

    int savePoint = nextCharIndex;

    if (peekChar() == '.') {
      nextChar();

      if (peekChar() == '.') {
        nextCharIndex = savePoint;

        String numberString = input.substring(beginIndex, nextCharIndex);

        return new LongToken(beginIndex, numberString, Long.parseLong(numberString));
      }
    }

    if (nextCharIndex != savePoint) {
      if (!collectSubsequentDigits())
        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS);

      String numberString = input.substring(beginIndex, nextCharIndex);

      return new DoubleToken(beginIndex, numberString, Double.parseDouble(numberString));
    }

    String numberString = input.substring(beginIndex, nextCharIndex);

    return new LongToken(beginIndex, numberString, Long.parseLong(numberString));
  }

  private @Nullable Token tryParseDotDoubleToken() {
    int beginIndex = nextCharIndex;

    if (nextChar() != '.')
      throw new IllegalStateException("Expected to only be called if nextChar() = '.'");

    if (!collectSubsequentDigits()) {
      nextCharIndex = beginIndex;
      return null;
    }

    String numberString = input.substring(beginIndex, nextCharIndex);

    return new DoubleToken(beginIndex, numberString, Double.parseDouble(numberString));
  }

  private @Nullable Token tryParseOperatorOrPunctuationToken() {
    int beginIndex = nextCharIndex;

    switch (nextChar()) {
      case '(':
        return new PunctuationToken(beginIndex, Punctuation.OPENING_PARENTHESIS);

      case ')':
        return new PunctuationToken(beginIndex, Punctuation.CLOSING_PARENTHESIS);

      case '[':
        return new InfixOperatorToken(beginIndex, InfixOperator.SUBSCRIPTING);

      case ']':
        return new PunctuationToken(beginIndex, Punctuation.CLOSING_BRACKET);

      case ',':
        return new PunctuationToken(beginIndex, Punctuation.COMMA);

      case '+':
        return new InfixOperatorToken(beginIndex, InfixOperator.ADDITION);

      case '-':
        if (dashIsPrefix)
          return new PrefixOperatorToken(beginIndex, PrefixOperator.FLIP_SIGN);

        return new InfixOperatorToken(beginIndex, InfixOperator.SUBTRACTION);

      case '*':
        return new InfixOperatorToken(beginIndex, InfixOperator.MULTIPLICATION);

      case '/':
        return new InfixOperatorToken(beginIndex, InfixOperator.DIVISION);

      case '%':
        return new InfixOperatorToken(beginIndex, InfixOperator.MODULO);

      case '^':
        return new InfixOperatorToken(beginIndex, InfixOperator.EXPONENTIATION);

      case '&':
        if (peekChar() == '&') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.CONJUNCTION);
        }

        return new InfixOperatorToken(beginIndex, InfixOperator.CONCATENATION);

      case '|':
        if (peekChar() == '|') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.DISJUNCTION);
        }

        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.SINGLE_PIPE);

      case '?':
        if (peekChar() == '?') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.NULL_COALESCE);
        }

        return new InfixOperatorToken(beginIndex, InfixOperator.BRANCHING);

      case ':':
        return new PunctuationToken(beginIndex, Punctuation.COLON);

      case '>':
        if (peekChar() == '=') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.GREATER_THAN_OR_EQUAL);
        }

        return new InfixOperatorToken(beginIndex, InfixOperator.GREATER_THAN);

      case '<':
        if (peekChar() == '=') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.LESS_THAN_OR_EQUAL);
        }

        return new InfixOperatorToken(beginIndex, InfixOperator.LESS_THAN);

      case '=':
        if (peekChar() == '=') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.EQUAL_TO);
        }

        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.SINGLE_EQUALS);

      case '!':
        if (peekChar() == '=') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.NOT_EQUAL_TO);
        }

        return new PrefixOperatorToken(beginIndex, PrefixOperator.NEGATION);

      case '.':
        char upcomingChar = peekChar();

        if (upcomingChar == '.') {
          nextChar();
          return new InfixOperatorToken(beginIndex, InfixOperator.RANGE);
        }

        if (upcomingChar >= '0' && upcomingChar <= '9') {
          nextCharIndex = beginIndex;
          return tryParseDotDoubleToken();
        }

        return new InfixOperatorToken(beginIndex, InfixOperator.MEMBER);
    }

    nextCharIndex = beginIndex;
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
    if (!this.pendingStack.isEmpty())
      return this.pendingStack.pop();

    while (Character.isWhitespace(peekChar()))
      nextChar();

    char upcomingChar = peekChar();

    if (upcomingChar == 0)
      return null;

    Token result;

    if (upcomingChar == '\'')
      result = parseStringToken();

    else if (upcomingChar >= '0' && upcomingChar <= '9')
      result = parseLongOrDoubleToken();

    else {
      if ((upcomingChar != '.' || (result = tryParseDotDoubleToken()) == null) && (result = tryParseOperatorOrPunctuationToken()) == null)
        result = parseIdentifierOrLiteralToken();
    }

    dashIsPrefix = result instanceof PrefixOperatorToken || result instanceof InfixOperatorToken;

    return result;
  }

  public @Nullable Token peekToken() {
    if (this.pendingStack.isEmpty())
      this.pendingStack.add(nextToken());

    return this.pendingStack.peek();
  }
}
