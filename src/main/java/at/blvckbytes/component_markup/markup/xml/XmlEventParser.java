package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.util.SubstringBuilder;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class XmlEventParser {

  private static final EnumSet<SubstringFlag> SUBSTRING_NOT_TEXT = EnumSet.noneOf(SubstringFlag.class);
  private static final EnumSet<SubstringFlag> SUBSTRING_INNER_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT);
  private static final EnumSet<SubstringFlag> SUBSTRING_FIRST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_LEADING_SPACE);
  private static final EnumSet<SubstringFlag> SUBSTRING_LAST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_TRAILING_SPACE);
  private static final EnumSet<SubstringFlag> SUBSTRING_ONLY_TEXT = EnumSet.allOf(SubstringFlag.class);

  private static final char[] TRUE_LITERAL_CHARS  = { 't', 'r', 'u', 'e' };
  private static final char[] FALSE_LITERAL_CHARS = { 'f', 'a', 'l', 's', 'e' };

  private final XmlEventConsumer consumer;
  private final SubstringBuilder substringBuilder;
  private final InputCursor cursor;

  private XmlEventParser(String input, XmlEventConsumer consumer) {
    this.consumer = consumer;
    this.substringBuilder = new SubstringBuilder(input);
    this.cursor = new InputCursor(input);
  }

  public static void parse(String input, XmlEventConsumer consumer) {
    new XmlEventParser(input, consumer).parseInput(false);
  }

  private final InStringDetector inStringDetector = new InStringDetector();

  private void parseInput(boolean isWithinCurlyBrackets) {
    char priorChar = 0;
    CursorPosition textContentBeginPosition = null;
    boolean wasPriorTagOrInterpolation = false;

    while (cursor.peekChar() != 0) {
      if (cursor.peekChar() == '}' && priorChar != '\\') {
        if (isWithinCurlyBrackets)
          break;

        throw new XmlParseException(XmlParseError.UNESCAPED_CLOSING_CURLY);
      }

      CursorPosition preConsumePosition = cursor.getPosition();
      int possibleNonTextBeginIndex = cursor.getNextCharIndex();

      if (cursor.peekChar() == '{') {
        cursor.nextChar();

        CursorPosition beginPosition = cursor.getPosition();

        if (cursor.peekChar() == '{') {
          cursor.nextChar();

          if (substringBuilder.hasStartSet()) {
            substringBuilder.setEndExclusive(possibleNonTextBeginIndex);
            consumer.onCursorPosition(textContentBeginPosition);

            consumer.onText(
              substringBuilder.build(
                wasPriorTagOrInterpolation
                  ? SUBSTRING_INNER_TEXT
                  : SUBSTRING_FIRST_TEXT
              )
            );
          }

          substringBuilder.setStartInclusive(cursor.getNextCharIndex());

          CursorPosition valueBeginPosition = null;

          while (cursor.peekChar() != 0) {
            int possiblePreTerminationIndex = cursor.getNextCharIndex();
            char currentChar = cursor.nextChar();

            if (currentChar == '\n') {
              consumer.onCursorPosition(beginPosition);
              throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION);
            }

            if (valueBeginPosition == null)
              valueBeginPosition = cursor.getPosition();

            inStringDetector.onEncounter(currentChar);

            if (inStringDetector.isInString())
              continue;

            if (currentChar == '}' && cursor.peekChar() == '}') {
              cursor.nextChar();
              substringBuilder.setEndExclusive(possiblePreTerminationIndex);
              break;
            }
          }

          consumer.onCursorPosition(beginPosition);

          if (!substringBuilder.hasEndSet())
            throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION);

          inStringDetector.reset();

          consumer.onInterpolation(substringBuilder.build(SUBSTRING_NOT_TEXT), valueBeginPosition);
          wasPriorTagOrInterpolation = true;
          continue;
        }

        cursor.restoreState(preConsumePosition);
      }

      cursor.consumeWhitespace();

      if (cursor.peekChar() == '<') {
        if (substringBuilder.hasStartSet()) {
          substringBuilder.setEndExclusive(possibleNonTextBeginIndex);
          consumer.onCursorPosition(textContentBeginPosition);

          consumer.onText(
            substringBuilder.build(
              wasPriorTagOrInterpolation
                ? SUBSTRING_INNER_TEXT
                : SUBSTRING_FIRST_TEXT
            )
          );
        }

        parseOpeningOrClosingTag();
        wasPriorTagOrInterpolation = true;
        priorChar = 0;
        continue;
      }

      cursor.restoreState(preConsumePosition);

      int nextCharIndex = cursor.getNextCharIndex();

      boolean beginContentText = !substringBuilder.hasStartSet();

      char nextChar = cursor.nextChar();

      if (beginContentText) {
        if (nextChar == '\n') {
          cursor.consumeWhitespace();
          continue;
        }

        substringBuilder.setStartInclusive(nextCharIndex);
        textContentBeginPosition = cursor.getPosition();
      }

      if (nextChar == '\\' && (cursor.peekChar() == '<' || cursor.peekChar() == '}')) {
        substringBuilder.addIndexToBeRemoved(nextCharIndex);
        nextChar = cursor.nextChar();
      }

      priorChar = nextChar;

      cursor.consumeWhitespace();
    }

    if (substringBuilder.hasStartSet()) {
      substringBuilder.setEndExclusive(cursor.getNextCharIndex());
      consumer.onCursorPosition(textContentBeginPosition);

      consumer.onText(
        substringBuilder.build(
          wasPriorTagOrInterpolation
            ? SUBSTRING_LAST_TEXT
            : SUBSTRING_ONLY_TEXT
        )
      );
    }

    if (!isWithinCurlyBrackets)
      consumer.onInputEnd();
  }

  private @Nullable String tryParseIdentifier(boolean emitState) {
    if (!isIdentifierChar(cursor.peekChar()))
      return null;

    substringBuilder.setStartInclusive(cursor.getNextCharIndex());

    cursor.nextChar();

    if (emitState)
      consumer.onCursorPosition(cursor.getPosition());

    while (isIdentifierChar(cursor.peekChar()))
      cursor.nextChar();

    substringBuilder.setEndExclusive(cursor.getNextCharIndex());

    return substringBuilder.build(SUBSTRING_NOT_TEXT);
  }

  private void parseAndEmitStringAttributeValue(String attributeName) {
    if (cursor.nextChar() != '"')
      throw new IllegalStateException("Expected opening double-quotes");

    CursorPosition valueBeginPosition = null;

    substringBuilder.setStartInclusive(cursor.getNextCharIndex());

    boolean encounteredEnd = false;
    char priorChar = 0;

    while (cursor.peekChar() != 0) {
      char currentChar = cursor.nextChar();

      if (currentChar == '\r' || currentChar == '\n')
        throw new XmlParseException(XmlParseError.UNTERMINATED_STRING);

      if (valueBeginPosition == null)
        valueBeginPosition = cursor.getPosition();

      if (currentChar == '"') {
        if (priorChar == '\\') {
          substringBuilder.addIndexToBeRemoved(cursor.getNextCharIndex() - 2);
        } else {
          substringBuilder.setEndExclusive(cursor.getNextCharIndex() - 1);
          encounteredEnd = true;
          break;
        }
      }

      priorChar = currentChar;
    }

    if (!encounteredEnd)
      throw new XmlParseException(XmlParseError.UNTERMINATED_STRING);

    String value = substringBuilder.build(SUBSTRING_NOT_TEXT);

    consumer.onStringAttribute(attributeName, valueBeginPosition, value);
  }

  private boolean doesEndOrHasTrailingWhiteSpaceOrTagTermination() {
    if (cursor.peekChar() == 0)
      return true;

    char peekedChar = cursor.peekChar();

    if (Character.isWhitespace(peekedChar))
      return true;

    return peekedChar == '>';
  }

  private void parseNumericAttributeValue(String attributeName) {
    substringBuilder.setStartInclusive(cursor.getNextCharIndex());

    if (cursor.peekChar() == '-')
      cursor.nextChar();

    cursor.consumeWhitespace();

    boolean encounteredDecimalPoint = false;
    boolean encounteredDigit = false;

    while (cursor.peekChar() != 0) {
      char peekedChar = cursor.peekChar();

      if (peekedChar >= '0' && peekedChar <= '9') {
        cursor.nextChar();
        encounteredDigit = true;
        continue;
      }

      if (peekedChar == '.') {
        if (encounteredDecimalPoint)
          throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);

        cursor.nextChar();
        encounteredDecimalPoint = true;
        continue;
      }

      break;
    }

    if (!encounteredDigit)
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);

    if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);

    substringBuilder.setEndExclusive(cursor.getNextCharIndex());
    String numberString = substringBuilder.build(SUBSTRING_NOT_TEXT);

    if (encounteredDecimalPoint) {
      try {
        consumer.onDoubleAttribute(attributeName, numberString, Double.parseDouble(numberString));
      } catch (NumberFormatException e) {
        throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);
      }
      return;
    }

    try {
      consumer.onLongAttribute(attributeName, numberString, Long.parseLong(numberString));
    } catch (NumberFormatException e) {
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER);
    }
  }

  private boolean tryParseAttribute() {
    cursor.consumeWhitespace();

    String attributeName = tryParseIdentifier(true);

    if (attributeName == null) {
      if (cursor.peekChar() == '"') {
        cursor.nextChar();
        consumer.onCursorPosition(cursor.getPosition());
        throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY);
      }

      return false;
    }

    // Attribute-identifiers do not support leading digits, as this constraint allows for
    // proper detection of malformed input; example: my-attr 53 (missing an equals-sign).
    if (Character.isDigit(attributeName.charAt(0)))
      throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY);

    cursor.consumeWhitespace();

    if (cursor.peekChar() != '=') {
      // These "keywords" are reserved; again - for proper detection of malformed input.
      if (attributeName.equalsIgnoreCase("true") || attributeName.equalsIgnoreCase("false"))
        throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY);

      consumer.onFlagAttribute(attributeName);
      return true;
    }

    cursor.nextChar();

    cursor.consumeWhitespace();

    switch (cursor.peekChar()) {
      case '"':
        parseAndEmitStringAttributeValue(attributeName);
        return true;

      case '{':
        cursor.nextChar();

        CursorPosition openingCurlyPosition = cursor.getPosition();

        consumer.onTagAttributeBegin(attributeName);

        parseInput(true);

        char nextChar = cursor.nextChar();

        if (nextChar != '}') {
          consumer.onCursorPosition(openingCurlyPosition);
          throw new XmlParseException(XmlParseError.UNTERMINATED_MARKUP_VALUE);
        }

        consumer.onCursorPosition(cursor.getPosition());
        consumer.onTagAttributeEnd(attributeName);
        return true;

      case 'T':
      case 't': {
        substringBuilder.setStartInclusive(cursor.getNextCharIndex());

        for (char c : TRUE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_TRUE);
        }

        substringBuilder.setEndExclusive(cursor.getNextCharIndex());

        if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
          throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_TRUE);

        consumer.onBooleanAttribute(attributeName, substringBuilder.build(SUBSTRING_NOT_TEXT), true);
        return true;
      }

      case 'F':
      case 'f':
        substringBuilder.setStartInclusive(cursor.getNextCharIndex());

        for (char c : FALSE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_FALSE);
        }

        substringBuilder.setEndExclusive(cursor.getNextCharIndex());

        if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
          throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_FALSE);

        consumer.onBooleanAttribute(attributeName, substringBuilder.build(SUBSTRING_NOT_TEXT), false);
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

  private boolean tryConsumeCommentTag(String tagName) {
    if (!tagName.equalsIgnoreCase("!--"))
      return false;

    CursorPosition savedPosition = cursor.getPosition();

    while (cursor.peekChar() != '-')
      cursor.nextChar();

    if (cursor.nextChar() != '-' || cursor.nextChar() != '-') {
      cursor.restoreState(savedPosition);
      return false;
    }

    if (cursor.nextChar() != '>') {
      cursor.restoreState(savedPosition);
      return false;
    }

    return true;
  }

  private void parseOpeningOrClosingTag() {
    if (cursor.nextChar() != '<')
      throw new IllegalStateException("Expected an opening pointy-bracket!");

    CursorPosition savedPosition = cursor.getPosition();

    cursor.consumeWhitespace();

    boolean wasClosingTag = false;

    if (cursor.peekChar() == '/') {
      cursor.nextChar();
      wasClosingTag = true;
    }

    String tagName = tryParseIdentifier(false);

    if (wasClosingTag) {
      consumer.onCursorPosition(savedPosition);

      if (cursor.nextChar() != '>')
        throw new XmlParseException(XmlParseError.UNTERMINATED_TAG);

      consumer.onTagClose(tagName);
      return;
    }

    if (tagName == null) {
      consumer.onCursorPosition(savedPosition);
      throw new XmlParseException(XmlParseError.MISSING_TAG_NAME);
    }

    if (tryConsumeCommentTag(tagName))
      return;

    consumer.onCursorPosition(savedPosition);

    consumer.onTagOpenBegin(tagName);

    while (cursor.peekChar() != 0) {
      if (!tryParseAttribute())
        break;
    }

    boolean wasSelfClosing = false;

    if (cursor.peekChar() == '/') {
      wasSelfClosing = true;
      cursor.nextChar();
    }

    cursor.consumeWhitespace();

    if (cursor.nextChar() != '>')
      throw new XmlParseException(XmlParseError.UNTERMINATED_TAG);

    consumer.onCursorPosition(cursor.getPosition());
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
