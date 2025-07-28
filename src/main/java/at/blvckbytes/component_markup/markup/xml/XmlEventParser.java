package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class XmlEventParser {

  private static final char[] TRUE_LITERAL_CHARS  = { 't', 'r', 'u', 'e' };
  private static final char[] FALSE_LITERAL_CHARS = { 'f', 'a', 'l', 's', 'e' };

  private final XmlEventConsumer consumer;
  private final TokenOutput tokenOutput;
  private final StringView input;

  private XmlEventParser(StringView input, XmlEventConsumer consumer, @Nullable TokenOutput tokenOutput) {
    this.consumer = consumer;
    this.tokenOutput = tokenOutput;
    this.input = input;
  }

  public static void parse(StringView input, XmlEventConsumer consumer) {
    new XmlEventParser(input, consumer, null).parseInput(false);
  }

  public static void parse(StringView input, XmlEventConsumer consumer, @Nullable TokenOutput tokenOutput) {
    new XmlEventParser(input, consumer, tokenOutput).parseInput(false);
  }

  private final InStringDetector inStringDetector = new InStringDetector();

  private void parseInput(boolean isWithinCurlyBrackets) {
    boolean wasPriorTagOrInterpolation = false;

    while (input.peekChar() != 0) {
      if (input.peekChar() == '}' && input.priorNextChar() != '\\') {
        if (isWithinCurlyBrackets)
          break;

        input.nextChar();

        throw new XmlParseException(XmlParseError.UNESCAPED_CURLY, input.getPosition());
      }

      int preConsumePosition = input.getPosition();

      if (input.peekChar() == '{' && input.priorNextChar() != '\\') {
        if (input.getSubViewStart() != -1) {
          emitText(
            input.buildSubViewInclusive(PositionMode.CURRENT),
            wasPriorTagOrInterpolation
              ? SubstringFlag.INNER_TEXT
              : SubstringFlag.FIRST_TEXT
          );
        }

        input.nextChar();
        input.setSubViewStart(input.getPosition());

        StringView interpolationValue = null;

        while (input.peekChar() != 0) {
          char currentChar = input.nextChar();

          if (currentChar == '\n' || currentChar == '{')
            throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION, input.getSubViewStart());

          if (tokenOutput != null && Character.isWhitespace(currentChar))
            tokenOutput.emitCharToken(input.getPosition(), TokenType.ANY__WHITESPACE);

          inStringDetector.onEncounter(currentChar);

          if (inStringDetector.isInString())
            continue;

          if (currentChar == '}') {
            interpolationValue = input.buildSubViewInclusive(PositionMode.CURRENT);
            break;
          }
        }

        inStringDetector.reset();

        if (interpolationValue == null)
          throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION, input.getSubViewStart());

        if (tokenOutput != null)
          tokenOutput.emitToken(TokenType.MARKUP__INTERPOLATION, interpolationValue);

        consumer.onInterpolation(interpolationValue.buildSubViewRelative(1, -1));

        wasPriorTagOrInterpolation = true;
        continue;
      }

      int firstSpacePosition = -1;

      if (input.peekChar() == ' ') {
        input.nextChar();
        firstSpacePosition = input.getPosition();

        if (tokenOutput != null)
          tokenOutput.emitCharToken(firstSpacePosition, TokenType.ANY__WHITESPACE);
      }

      boolean encounteredNewline = input.consumeWhitespaceAndGetIfNewline(tokenOutput);

      if (input.peekChar() == '<') {
        if (input.getSubViewStart() != -1) {
          emitText(
            input.buildSubViewInclusive(PositionMode.CURRENT),
            wasPriorTagOrInterpolation
              ? SubstringFlag.INNER_TEXT
              : SubstringFlag.FIRST_TEXT
          );
        }

        else if (wasPriorTagOrInterpolation && firstSpacePosition != -1 && !encounteredNewline) {
          input.setSubViewStart(firstSpacePosition);
          emitText(input.buildSubViewInclusive(PositionMode.CURRENT), SubstringFlag.INNER_TEXT);
        }

        parseOpeningOrClosingTag();
        wasPriorTagOrInterpolation = true;
        continue;
      }

      input.restorePosition(preConsumePosition);

      char currentChar = input.nextChar();

      if (input.getSubViewStart() == -1) {
        if (currentChar == '\n') {
          input.consumeWhitespaceAndGetIfNewline(tokenOutput);
          continue;
        }

        input.setSubViewStart(input.getPosition());
      }

      if (currentChar == '\\' && (input.peekChar() == '<' || input.peekChar() == '}' || input.peekChar() == '{')) {
        input.addIndexToBeRemoved(input.getCharIndex());
        input.nextChar();
      }

      input.consumeWhitespaceAndGetIfNewline(tokenOutput);
    }

    if (input.getSubViewStart() != -1) {
      emitText(
        input.buildSubViewInclusive(PositionMode.CURRENT),
        wasPriorTagOrInterpolation
          ? SubstringFlag.LAST_TEXT
          : SubstringFlag.ONLY_TEXT
      );
    }

    if (!isWithinCurlyBrackets) {
      if (input.getSubViewStart() != -1)
        throw new IllegalStateException("There was still a sub-view set after encountering the input's end");

      consumer.onInputEnd();
    }
  }

  private void emitText(StringView text, @Nullable EnumSet<SubstringFlag> flags) {
    if (flags != null)
      text.setBuildFlags(flags);

    if (text.buildString().isEmpty())
      return;

    consumer.onText(text);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__PLAIN_TEXT, text);
  }

  private @Nullable StringView tryParseIdentifier() {
    if (!isIdentifierChar(input.peekChar()))
      return null;

    input.nextChar();
    input.setSubViewStart(input.getPosition());

    while (isIdentifierChar(input.peekChar()))
      input.nextChar();

    return input.buildSubViewInclusive(PositionMode.CURRENT);
  }

  private void parseAndEmitStringAttributeValue(StringView attributeName) {
    if (input.nextChar() != '"')
      throw new IllegalStateException("Expected opening double-quotes");

    input.setSubViewStart(input.getPosition());

    StringView value = null;

    char currentChar;

    while ((currentChar = input.nextChar()) != 0) {
      if (currentChar == '\r' || currentChar == '\n')
        throw new XmlParseException(XmlParseError.UNTERMINATED_STRING, input.getSubViewStart());

      if (currentChar == '"') {
        if (input.priorNextChar() == '\\') {
          input.addIndexToBeRemoved(input.getCharIndex() - 1);
        } else {
          value = input.buildSubViewInclusive(PositionMode.CURRENT);
          break;
        }
      }

      if (tokenOutput != null && Character.isWhitespace(currentChar))
        tokenOutput.emitCharToken(input.getPosition(), TokenType.ANY__WHITESPACE);
    }

    if (value == null)
      throw new XmlParseException(XmlParseError.UNTERMINATED_STRING, input.getSubViewStart());

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__STRING, value);

    consumer.onStringAttribute(attributeName, value.buildSubViewRelative(1, -1));
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean doesEndOrHasTrailingWhiteSpaceOrTagTermination() {
    if (input.peekChar() == 0)
      return true;

    char peekedChar = input.peekChar();

    if (Character.isWhitespace(peekedChar))
      return true;

    return peekedChar == '>';
  }

  private void parseNumericAttributeValue(StringView attributeName) {
    if (input.peekChar() == '-') {
      input.nextChar();
      input.setSubViewStart(input.getPosition());
    }

    boolean encounteredDecimalPoint = false;
    boolean encounteredDigit = false;

    while (input.peekChar() != 0) {
      char peekedChar = input.peekChar();

      if (peekedChar >= '0' && peekedChar <= '9') {
        input.nextChar();

        if (input.getSubViewStart() == -1)
          input.setSubViewStart(input.getPosition());

        encounteredDigit = true;
        continue;
      }

      if (peekedChar == '.') {
        if (encounteredDecimalPoint)
          throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, input.getSubViewStart());

        input.nextChar();

        if (input.getSubViewStart() == -1)
          input.setSubViewStart(input.getPosition());

        encounteredDecimalPoint = true;
        continue;
      }

      break;
    }

    int start = input.getSubViewStart();

    if (!encounteredDigit)
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, start);

    if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, start);

    StringView value = input.buildSubViewInclusive(PositionMode.CURRENT);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__NUMBER, value);

    if (encounteredDecimalPoint) {
      try {
        consumer.onDoubleAttribute(attributeName, value, Double.parseDouble(value.buildString()));
      } catch (NumberFormatException e) {
        throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, start);
      }
      return;
    }

    try {
      consumer.onLongAttribute(attributeName, value, Long.parseLong(value.buildString()));
    } catch (NumberFormatException e) {
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, start);
    }
  }

  private boolean tryParseAttribute() {
    input.consumeWhitespaceAndGetIfNewline(tokenOutput);

    // Attribute-name tokens are emitted by the markup-parser, which knows more about the details
    StringView attributeName = tryParseIdentifier();

    if (attributeName == null) {
      if (input.peekChar() == '"') {
        input.nextChar();
        throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY, input.getPosition());
      }

      return false;
    }

    // Attribute-identifiers do not support leading digits, as this constraint allows for
    // proper detection of malformed input; example: my-attr 53 (missing an equals-sign).
    if (Character.isDigit(attributeName.nthChar(0)))
      throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY, attributeName.startInclusive);

    input.consumeWhitespaceAndGetIfNewline(tokenOutput);

    if (input.peekChar() != '=') {
      // These "keywords" are reserved; again - for proper detection of malformed input.
      if (attributeName.contentEquals("true", true) || attributeName.contentEquals("false", true))
        throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY, attributeName.startInclusive);

      consumer.onFlagAttribute(attributeName);
      return true;
    }

    input.nextChar();

    if (tokenOutput != null)
      tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__EQUALS);

    input.consumeWhitespaceAndGetIfNewline(tokenOutput);

    switch (input.peekChar()) {
      case '"':
        parseAndEmitStringAttributeValue(attributeName);
        return true;

      case '{':
        input.nextChar();

        int openingCurlyPosition = input.getPosition();

        if (tokenOutput != null)
          tokenOutput.emitCharToken(openingCurlyPosition, TokenType.MARKUP__PUNCTUATION__SUBTREE);

        consumer.onTagAttributeBegin(attributeName, openingCurlyPosition);

        parseInput(true);

        char nextChar = input.nextChar();

        if (nextChar != '}')
          throw new XmlParseException(XmlParseError.UNTERMINATED_MARKUP_VALUE, openingCurlyPosition);

        int closingCurlyPosition = input.getPosition();

        if (tokenOutput != null)
          tokenOutput.emitCharToken(closingCurlyPosition, TokenType.MARKUP__PUNCTUATION__SUBTREE);

        consumer.onTagAttributeEnd(attributeName);
        return true;

      case 'T':
      case 't':
        consumer.onBooleanAttribute(attributeName, parseLiteral(attributeName, TRUE_LITERAL_CHARS, XmlParseError.MALFORMED_LITERAL_TRUE), true);
        return true;

      case 'F':
      case 'f':
        consumer.onBooleanAttribute(attributeName, parseLiteral(attributeName, FALSE_LITERAL_CHARS, XmlParseError.MALFORMED_LITERAL_FALSE), false);
        return true;

      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
      case '.':
      case '-':
        parseNumericAttributeValue(attributeName);
        return true;

      default:
        throw new XmlParseException(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, input.getPosition());
    }
  }

  private StringView parseLiteral(StringView attributeName, char[] chars, XmlParseError error) {
    int begin = -1;

    for (char c : chars) {
      if (Character.toLowerCase(input.nextChar()) != c)
        throw new XmlParseException(error, attributeName.startInclusive);

      if (begin == -1)
        begin = input.getPosition();
    }

    if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
      throw new XmlParseException(error, begin);

    input.setSubViewStart(begin);
    StringView value = input.buildSubViewInclusive(PositionMode.CURRENT);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__LITERAL__ANY, value);

    return value;
  }

  private @Nullable StringView tryConsumeCommentTag(StringView tagName) {
    if (!tagName.contentEquals("!--", true))
      return null;

    int savedPosition = input.getPosition();

    while (input.peekChar() != '-')
      input.nextChar();

    if (input.nextChar() != '-' || input.nextChar() != '-') {
      input.restorePosition(savedPosition);
      return null;
    }

    if (input.nextChar() != '>') {
      input.restorePosition(savedPosition);
      return null;
    }

    input.setSubViewStart(tagName.startInclusive);
    return input.buildSubViewInclusive(PositionMode.CURRENT);
  }

  private void parseOpeningOrClosingTag() {
    if (input.nextChar() != '<')
      throw new IllegalStateException("Expected an opening pointy-bracket!");

    int openingPosition = input.getPosition();

    if (tokenOutput != null)
      tokenOutput.emitCharToken(openingPosition, TokenType.MARKUP__PUNCTUATION__TAG);

    input.consumeWhitespaceAndGetIfNewline(tokenOutput);

    boolean wasClosingTag = false;

    if (input.peekChar() == '/') {
      input.nextChar();

      if (tokenOutput != null)
        tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__TAG);

      wasClosingTag = true;
    }

    input.consumeWhitespaceAndGetIfNewline(tokenOutput);

    StringView tagName = tryParseIdentifier();

    if (wasClosingTag) {
      if (input.nextChar() != '>')
        throw new XmlParseException(XmlParseError.UNTERMINATED_TAG, openingPosition);

      if (tokenOutput != null) {
        if (tagName != null)
          tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__TAG, tagName);

        tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__TAG);
      }

      consumer.onTagClose(tagName, openingPosition);
      return;
    }

    if (tagName == null)
      throw new XmlParseException(XmlParseError.MISSING_TAG_NAME, openingPosition);

    StringView comment;

    if ((comment = tryConsumeCommentTag(tagName)) != null) {
      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__COMMENT, comment);

      return;
    }

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__TAG, tagName);

    consumer.onTagOpenBegin(tagName);

    while (input.peekChar() != 0) {
      if (!tryParseAttribute())
        break;
    }

    boolean wasSelfClosing = false;

    if (input.peekChar() == '/') {
      input.nextChar();
      wasSelfClosing = true;

      if (tokenOutput != null)
        tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__TAG);
    }

    input.consumeWhitespaceAndGetIfNewline(tokenOutput);

    if (input.nextChar() != '>')
      throw new XmlParseException(XmlParseError.UNTERMINATED_TAG, tagName.startInclusive);

    int closingPosition = input.getPosition();

    if (tokenOutput != null)
      tokenOutput.emitCharToken(closingPosition, TokenType.MARKUP__PUNCTUATION__TAG);

    consumer.onTagOpenEnd(tagName, wasSelfClosing);
  }

  private boolean isIdentifierChar(char c) {
    if (c < '!' || c > '~')
      return false;

    switch (c) {
      case '<':
      case '>':
      case '{':
      case '}':
      case '\\':
      case '/':
      case '\'':
      case '"':
      case '=':
        return false;
      default:
        return true;
    }
  }
}
