package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class ExpressionTokenizer {

  private final Stack<ExpressionToken> pendingStack;
  private final String input;
  private int nextCharIndex;

  public ExpressionTokenizer(String input) {
    this.pendingStack = new Stack<>();
    this.input = input;
    this.nextCharIndex = 0;
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

  private ExpressionToken parseStringToken() {
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

  private ExpressionToken parseIdentifierOrLiteralToken() {
    int beginIndex = nextCharIndex;
    int endIndexExclusive = beginIndex;

    char upcomingChar;
    char priorChar = 0;

    while ((upcomingChar = peekChar()) != 0) {
      if (Character.isWhitespace(upcomingChar))
        break;

      ExpressionToken upcomingOperatorOrPunctuation;

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
        return new BooleanToken(beginIndex, true);

      case "false":
        return new BooleanToken(beginIndex, false);

      case "null":
        return new NullToken(beginIndex);
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

  private ExpressionToken parseLongOrDoubleToken() {
    int beginIndex = nextCharIndex;

    if (!collectSubsequentDigits())
      throw new IllegalStateException("Expected to only be called if nextChar() is a digit");

    int savePoint = nextCharIndex;

    if (peekChar() == '.') {
      nextChar();

      if (peekChar() == '.') {
        nextCharIndex = savePoint;
        return new LongToken(beginIndex, Long.parseLong(input.substring(beginIndex, nextCharIndex)));
      }
    }

    if (nextCharIndex != savePoint) {
      if (!collectSubsequentDigits())
        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.EXPECTED_DECIMAL_DIGITS);

      return new DoubleToken(beginIndex, Double.parseDouble(input.substring(beginIndex, nextCharIndex)));
    }

    return new LongToken(beginIndex, Long.parseLong(input.substring(beginIndex, nextCharIndex)));
  }

  private @Nullable ExpressionToken tryParseDotDoubleToken() {
    int beginIndex = nextCharIndex;

    if (nextChar() != '.')
      throw new IllegalStateException("Expected to only be called if nextChar() = '.'");

    if (!collectSubsequentDigits()) {
      nextCharIndex = beginIndex;
      return null;
    }

    return new DoubleToken(beginIndex, Double.parseDouble(input.substring(beginIndex, nextCharIndex)));
  }

  private @Nullable ExpressionToken tryParseOperatorOrPunctuationToken() {
    int beginIndex = nextCharIndex;

    switch (nextChar()) {
      case '(':
        return new PunctuationToken(beginIndex, Punctuation.OPENING_PARENTHESIS);

      case ')':
        return new PunctuationToken(beginIndex, Punctuation.CLOSING_PARENTHESIS);

      case '[':
        return new PunctuationToken(beginIndex, Punctuation.OPENING_BRACKET);

      case ']':
        return new PunctuationToken(beginIndex, Punctuation.CLOSING_BRACKET);

      case ',':
        return new PunctuationToken(beginIndex, Punctuation.COMMA);

      case '+':
        return new OperatorToken(beginIndex, Operator.ADDITION);

      case '-':
        return new OperatorToken(beginIndex, Operator.SUBTRACTION);

      case '*':
        return new OperatorToken(beginIndex, Operator.MULTIPLICATION);

      case '/':
        return new OperatorToken(beginIndex, Operator.DIVISION);

      case '%':
        return new OperatorToken(beginIndex, Operator.MODULO);

      case '^':
        return new OperatorToken(beginIndex, Operator.EXPONENTIATION);

      case '&':
        if (peekChar() == '&') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.CONJUNCTION);
        }

        return new OperatorToken(beginIndex, Operator.CONCATENATION);

      case '|':
        if (peekChar() == '|') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.DISJUNCTION);
        }

        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.SINGLE_PIPE);

      case '?':
        if (peekChar() == '?') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.NULL_COALESCE);
        }

        return new OperatorToken(beginIndex, Operator.TERNARY_BEGIN);

      case ':':
        return new OperatorToken(beginIndex, Operator.TERNARY_DELIMITER);

      case '>':
        if (peekChar() == '=') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.GREATER_THAN_OR_EQUAL);
        }

        return new OperatorToken(beginIndex, Operator.GREATER_THAN);

      case '<':
        if (peekChar() == '=') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.LESS_THAN_OR_EQUAL);
        }

        return new OperatorToken(beginIndex, Operator.LESS_THAN);

      case '=':
        if (peekChar() == '=') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.EQUAL_TO);
        }

        throw new ExpressionTokenizeException(beginIndex, ExpressionTokenizeError.SINGLE_EQUALS);

      case '!':
        if (peekChar() == '=') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.NOT_EQUAL_TO);
        }

        return new OperatorToken(beginIndex, Operator.NEGATION);

      case '.':
        char upcomingChar = peekChar();

        if (upcomingChar == '.') {
          nextChar();
          return new OperatorToken(beginIndex, Operator.RANGE);
        }

        if (upcomingChar >= '0' && upcomingChar <= '9') {
          nextCharIndex = beginIndex;
          return tryParseDotDoubleToken();
        }

        return new OperatorToken(beginIndex, Operator.MEMBER);
    }

    nextCharIndex = beginIndex;
    return null;
  }

  public @Nullable ExpressionToken nextToken() {
    if (!this.pendingStack.isEmpty())
      return this.pendingStack.pop();

    ExpressionToken result;

    while (Character.isWhitespace(peekChar()))
      nextChar();

    char upcomingChar = peekChar();

    if (upcomingChar == 0)
      return null;

    if (upcomingChar == '\'')
      return parseStringToken();

    if (upcomingChar >= '0' && upcomingChar <= '9')
      return parseLongOrDoubleToken();

    if (upcomingChar == '.') {
      if ((result = tryParseDotDoubleToken()) != null)
        return result;
    }

    if ((result = tryParseOperatorOrPunctuationToken()) != null)
      return result;

    return parseIdentifierOrLiteralToken();
  }

  public @Nullable ExpressionToken peekToken() {
    if (this.pendingStack.isEmpty())
      this.pendingStack.add(nextToken());

    return this.pendingStack.peek();
  }
}
