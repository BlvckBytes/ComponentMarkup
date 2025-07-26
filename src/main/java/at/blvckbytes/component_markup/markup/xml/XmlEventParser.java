package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.PositionMode;
import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class XmlEventParser {

  private static final EnumSet<SubstringFlag> SUBSTRING_INNER_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT);
  private static final EnumSet<SubstringFlag> SUBSTRING_FIRST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_LEADING_SPACE);
  private static final EnumSet<SubstringFlag> SUBSTRING_LAST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_TRAILING_SPACE);
  private static final EnumSet<SubstringFlag> SUBSTRING_ONLY_TEXT = EnumSet.allOf(SubstringFlag.class);

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
      if (input.peekChar() == '}' && input.priorChar() != '\\') {
        if (isWithinCurlyBrackets)
          break;

        throw new XmlParseException(XmlParseError.UNESCAPED_CURLY);
      }

      StringPosition preConsumePosition = input.getPosition();

      if (input.peekChar() == '{' && input.priorChar() != '\\') {
        if (input.getSubViewStart() != null) {
          emitText(
            input.buildSubViewUntilPosition(PositionMode.CURRENT),
            wasPriorTagOrInterpolation ? SUBSTRING_INNER_TEXT : SUBSTRING_FIRST_TEXT
          );
        }

        input.nextChar();
        input.setSubViewStart(input.getPosition());

        StringView interpolationValue = null;

        while (input.peekChar() != 0) {
          char currentChar = input.nextChar();

          if (currentChar == '\n' || currentChar == '{') {
            consumer.onPosition(input.getSubViewStart());
            throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION);
          }

          if (tokenOutput != null && Character.isWhitespace(currentChar))
            tokenOutput.emitCharToken(input.getPosition(), TokenType.ANY__WHITESPACE);

          inStringDetector.onEncounter(currentChar);

          if (inStringDetector.isInString())
            continue;

          if (currentChar == '}') {
            interpolationValue = input.buildSubViewUntilPosition(PositionMode.CURRENT);
            break;
          }
        }

        inStringDetector.reset();

        if (interpolationValue == null) {
          consumer.onPosition(input.getSubViewStart());
          throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION);
        }

        if (tokenOutput != null)
          tokenOutput.emitToken(TokenType.MARKUP__INTERPOLATION, interpolationValue);

        consumer.onInterpolation(interpolationValue.buildSubViewRelative(1, -1));

        wasPriorTagOrInterpolation = true;
        continue;
      }

      StringPosition firstSpacePosition = null;

      if (input.peekChar() == ' ') {
        input.nextChar();
        firstSpacePosition = input.getPosition();

        if (tokenOutput != null)
          tokenOutput.emitCharToken(firstSpacePosition, TokenType.ANY__WHITESPACE);
      }

      input.consumeWhitespace(tokenOutput);

      if (input.peekChar() == '<') {
        if (input.getSubViewStart() != null) {
          emitText(
            input.buildSubViewUntilPosition(PositionMode.CURRENT),
            wasPriorTagOrInterpolation
              ? SUBSTRING_INNER_TEXT
              : SUBSTRING_FIRST_TEXT
          );
        }

        else if (wasPriorTagOrInterpolation && firstSpacePosition != null && firstSpacePosition.lineNumber == input.getLineNumber()) {
          input.setSubViewStart(preConsumePosition);
          emitText(input.buildSubViewUntilPosition(PositionMode.CURRENT), SubstringFlag.NONE);
        }

        parseOpeningOrClosingTag();
        wasPriorTagOrInterpolation = true;
        continue;
      }

      input.restorePosition(preConsumePosition);

      char currentChar = input.nextChar();

      if (input.getSubViewStart() == null) {
        if (currentChar == '\n') {
          input.consumeWhitespace(tokenOutput);
          continue;
        }

        input.setSubViewStart(input.getPosition(PositionMode.NEXT));
      }

      if (currentChar == '\\' && (input.peekChar() == '<' || input.peekChar() == '}' || input.peekChar() == '{'))
        input.addIndexToBeRemoved(input.getCharIndex());

      input.consumeWhitespace(tokenOutput);
    }

    if (input.getSubViewStart() != null) {
      emitText(
        input.buildSubViewUntilPosition(PositionMode.CURRENT),
        wasPriorTagOrInterpolation
          ? SUBSTRING_LAST_TEXT
          : SUBSTRING_ONLY_TEXT
      );
    }

    if (!isWithinCurlyBrackets)
      consumer.onInputEnd();
  }

  private void emitText(StringView text, EnumSet<SubstringFlag> flags) {
    consumer.onPosition(text.viewStart);
    consumer.onText(text, flags);

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

    return input.buildSubViewUntilPosition(PositionMode.CURRENT);
  }

  private void parseAndEmitStringAttributeValue(StringView attributeName) {
    if (input.nextChar() != '"')
      throw new IllegalStateException("Expected opening double-quotes");

    input.setSubViewStart(input.getPosition());

    StringView value = null;

    char currentChar;

    while ((currentChar = input.nextChar()) != 0) {
      if (currentChar == '\r' || currentChar == '\n')
        throw new XmlParseException(XmlParseError.UNTERMINATED_STRING);

      if (currentChar == '"') {
        if (input.priorChar() == '\\') {
          input.addIndexToBeRemoved(input.getCharIndex() - 1);
        } else {
          value = input.buildSubViewUntilPosition(PositionMode.CURRENT);
          break;
        }
      }

      if (tokenOutput != null && Character.isWhitespace(currentChar))
        tokenOutput.emitCharToken(input.getPosition(), TokenType.ANY__WHITESPACE);
    }

    if (value == null)
      throw new XmlParseException(XmlParseError.UNTERMINATED_STRING);

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

        if (input.getSubViewStart() == null)
          input.setSubViewStart(input.getPosition());

        encounteredDigit = true;
        continue;
      }

      if (peekedChar == '.') {
        if (encounteredDecimalPoint)
          throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);

        input.nextChar();

        if (input.getSubViewStart() == null)
          input.setSubViewStart(input.getPosition());

        encounteredDecimalPoint = true;
        continue;
      }

      break;
    }

    if (!encounteredDigit)
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);

    if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);

    StringView value = input.buildSubViewUntilPosition(PositionMode.CURRENT);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__NUMBER, value);

    if (encounteredDecimalPoint) {
      try {
        consumer.onDoubleAttribute(attributeName, value, value.parseDouble());
      } catch (NumberFormatException e) {
        throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);
      }
      return;
    }

    try {
      consumer.onLongAttribute(attributeName, value, value.parseLong());
    } catch (NumberFormatException e) {
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);
    }
  }

  private boolean tryParseAttribute() {
    input.consumeWhitespace(tokenOutput);

    // Attribute-name tokens are emitted by the markup-parser, which knows more about the details
    StringView attributeName = tryParseIdentifier();

    if (attributeName == null) {
      if (input.peekChar() == '"') {
        input.nextChar();
        consumer.onPosition(input.getPosition());
        throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY);
      }

      return false;
    }

    consumer.onPosition(attributeName.getPosition());

    // Attribute-identifiers do not support leading digits, as this constraint allows for
    // proper detection of malformed input; example: my-attr 53 (missing an equals-sign).
    if (Character.isDigit(attributeName.nthChar(0)))
      throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY);

    input.consumeWhitespace(tokenOutput);

    if (input.peekChar() != '=') {
      // These "keywords" are reserved; again - for proper detection of malformed input.
      if (attributeName.contentEquals("true", true) || attributeName.contentEquals("false", true))
        throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY);

      consumer.onFlagAttribute(attributeName);
      return true;
    }

    input.nextChar();

    if (tokenOutput != null)
      tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__EQUALS);

    input.consumeWhitespace(tokenOutput);

    switch (input.peekChar()) {
      case '"':
        parseAndEmitStringAttributeValue(attributeName);
        return true;

      case '{':
        input.nextChar();

        StringPosition openingCurlyPosition = input.getPosition();

        if (tokenOutput != null)
          tokenOutput.emitCharToken(openingCurlyPosition, TokenType.MARKUP__PUNCTUATION__SUBTREE);

        consumer.onTagAttributeBegin(attributeName);

        parseInput(true);

        char nextChar = input.nextChar();

        if (nextChar != '}') {
          consumer.onPosition(openingCurlyPosition);
          throw new XmlParseException(XmlParseError.UNTERMINATED_MARKUP_VALUE);
        }

        StringPosition closingCurlyPosition = input.getPosition();

        if (tokenOutput != null)
          tokenOutput.emitCharToken(closingCurlyPosition, TokenType.MARKUP__PUNCTUATION__SUBTREE);

        consumer.onPosition(closingCurlyPosition);
        consumer.onTagAttributeEnd(attributeName);
        return true;

      case 'T':
      case 't':
        consumer.onBooleanAttribute(attributeName, parseLiteral(TRUE_LITERAL_CHARS, XmlParseError.MALFORMED_LITERAL_TRUE), true);
        return true;

      case 'F':
      case 'f':
        consumer.onBooleanAttribute(attributeName, parseLiteral(FALSE_LITERAL_CHARS, XmlParseError.MALFORMED_LITERAL_FALSE), false);
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
        throw new XmlParseException(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE);
    }
  }

  private StringView parseLiteral(char[] chars, XmlParseError error) {
    StringPosition begin = null;

    for (char c : chars) {
      if (Character.toLowerCase(input.nextChar()) != c)
        throw new XmlParseException(error);

      if (begin == null)
        begin = input.getPosition();
    }

    // Let's assume that we're not passing empty literals...
    assert begin != null;

    if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
      throw new XmlParseException(error);

    input.setSubViewStart(begin);
    StringView value = input.buildSubViewUntilPosition(PositionMode.CURRENT);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__LITERAL__ANY, value);

    return value;
  }

  private @Nullable StringView tryConsumeCommentTag(StringView tagName) {
    if (!tagName.contentEquals("!--", true))
      return null;

    StringPosition savedPosition = input.getPosition();

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

    input.setSubViewStart(tagName.viewStart);
    return input.buildSubViewUntilPosition(PositionMode.CURRENT);
  }

  private void parseOpeningOrClosingTag() {
    if (input.nextChar() != '<')
      throw new IllegalStateException("Expected an opening pointy-bracket!");

    StringPosition openingPosition = input.getPosition();

    if (tokenOutput != null)
      tokenOutput.emitCharToken(openingPosition, TokenType.MARKUP__PUNCTUATION__TAG);

    input.consumeWhitespace(tokenOutput);

    boolean wasClosingTag = false;

    if (input.peekChar() == '/') {
      if (tokenOutput != null)
        tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__TAG);

      input.nextChar();
      wasClosingTag = true;
    }

    input.consumeWhitespace(tokenOutput);

    StringView tagName = tryParseIdentifier();

    if (wasClosingTag) {
      consumer.onPosition(openingPosition);

      if (input.nextChar() != '>')
        throw new XmlParseException(XmlParseError.UNTERMINATED_TAG);

      if (tokenOutput != null) {
        if (tagName != null)
          tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__TAG, tagName);

        tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__TAG);
      }

      consumer.onTagClose(tagName);
      return;
    }

    if (tagName == null) {
      consumer.onPosition(openingPosition);
      throw new XmlParseException(XmlParseError.MISSING_TAG_NAME);
    }

    StringView comment;

    if ((comment = tryConsumeCommentTag(tagName)) != null) {
      if (tokenOutput != null)
        tokenOutput.emitToken(TokenType.MARKUP__COMMENT, comment);

      return;
    }

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__TAG, tagName);

    consumer.onPosition(openingPosition);
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

    input.consumeWhitespace(tokenOutput);

    if (input.nextChar() != '>')
      throw new XmlParseException(XmlParseError.UNTERMINATED_TAG);

    StringPosition closingPosition = input.getPosition();

    if (tokenOutput != null)
      tokenOutput.emitCharToken(closingPosition, TokenType.MARKUP__PUNCTUATION__TAG);

    consumer.onPosition(closingPosition);
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
