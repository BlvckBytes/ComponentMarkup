package at.blvckbytes.component_markup.xml;

import org.jetbrains.annotations.Nullable;

public class XmlEventParser {

  private static final char[] TRUE_LITERAL_CHARS  = { 't', 'r', 'u', 'e' };
  private static final char[] FALSE_LITERAL_CHARS = { 'f', 'a', 'l', 's', 'e' };

  private final XmlEventConsumer consumer;
  private final SubstringBuilder substringBuilder;
  private final InputCursor cursor;

  private XmlEventParser(String input, XmlEventConsumer consumer) {
    this.consumer = consumer;
    this.substringBuilder = new SubstringBuilder(input);
    this.cursor = new InputCursor(input, consumer);
  }

  public static void parse(String input, XmlEventConsumer consumer) {
    new XmlEventParser(input, consumer).parseInput(false);
  }

  private final InStringDetector inStringDetector = new InStringDetector();

  private void parseInput(boolean isWithinCurlyBrackets) {
    char priorChar = 0;
    long textContentBeginState = -1;

    while (cursor.hasRemainingChars()) {
      if (cursor.peekChar() == '}' && priorChar != '\\') {
        if (isWithinCurlyBrackets)
          break;

        throw new XmlParseException(ParseError.UNESCAPED_CLOSING_CURLY);
      }

      long preConsumeState = cursor.getState();
      int possibleNonTextBeginIndex = cursor.getNextCharIndex();

      if (cursor.peekChar() == '{') {
        cursor.nextChar();

        long beginState = cursor.getState();

        if (cursor.peekChar() == '{') {
          cursor.nextChar();

          if (substringBuilder.hasStartSet()) {
            substringBuilder.setEndExclusive(possibleNonTextBeginIndex);
            cursor.emitState(textContentBeginState);
            consumer.onText(substringBuilder.build(true));
          }

          substringBuilder.setStartInclusive(cursor.getNextCharIndex());

          while (cursor.hasRemainingChars()) {
            int possiblePreTerminationIndex = cursor.getNextCharIndex();
            char c = cursor.nextChar();

            inStringDetector.onEncounter(c);

            if (inStringDetector.isInString())
              continue;

            if (c == '}' && cursor.peekChar() == '}') {
              cursor.nextChar();
              substringBuilder.setEndExclusive(possiblePreTerminationIndex);
              break;
            }
          }

          cursor.emitState(beginState);

          if (!substringBuilder.hasEndSet())
            throw new XmlParseException(ParseError.UNTERMINATED_INTERPOLATION);

          inStringDetector.reset();

          String expression = substringBuilder.build(false);

          consumer.onInterpolation(expression);
          continue;
        }

        cursor.restoreState(preConsumeState);
      }

      cursor.consumeWhitespace();

      if (cursor.peekChar() == '<') {
        if (substringBuilder.hasStartSet()) {
          substringBuilder.setEndExclusive(possibleNonTextBeginIndex);
          cursor.emitState(textContentBeginState);
          consumer.onText(substringBuilder.build(true));
        }

        parseOpeningOrClosingTag();
        priorChar = 0;
        continue;
      }

      cursor.restoreState(preConsumeState);

      int nextCharIndex = cursor.getNextCharIndex();

      boolean beginContentText = !substringBuilder.hasStartSet();

      char nextChar = cursor.nextChar();

      if (beginContentText) {
        if (nextChar == '\n') {
          cursor.consumeWhitespace();
          continue;
        }

        substringBuilder.setStartInclusive(nextCharIndex);
        textContentBeginState = cursor.getState();
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

      String trailingText = substringBuilder.build(true);

      if (!trailingText.trim().isEmpty()) {
        cursor.emitState(textContentBeginState);
        consumer.onText(trailingText);
      }
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
      cursor.emitCurrentState();

    while (isIdentifierChar(cursor.peekChar()))
      cursor.nextChar();

    substringBuilder.setEndExclusive(cursor.getNextCharIndex());

    return substringBuilder.build(false);
  }

  private String parseStringAttributeValue() {
    if (cursor.nextChar() != '"')
      throw new IllegalStateException("Expected opening double-quotes");

    substringBuilder.setStartInclusive(cursor.getNextCharIndex());

    boolean encounteredEnd = false;
    char priorChar = 0;

    while (cursor.hasRemainingChars()) {
      char currentChar = cursor.nextChar();

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
      throw new XmlParseException(ParseError.UNTERMINATED_STRING);

    return substringBuilder.build(false);
  }

  private boolean doesEndOrHasTrailingWhiteSpaceOrTagTermination() {
    if (!cursor.hasRemainingChars())
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

    while (cursor.hasRemainingChars()) {
      char peekedChar = cursor.peekChar();

      if (peekedChar >= '0' && peekedChar <= '9') {
        cursor.nextChar();
        encounteredDigit = true;
        continue;
      }

      if (peekedChar == '.') {
        if (encounteredDecimalPoint)
          throw new XmlParseException(ParseError.MALFORMED_NUMBER);

        cursor.nextChar();
        encounteredDecimalPoint = true;
        continue;
      }

      break;
    }

    if (!encounteredDigit)
      throw new XmlParseException(ParseError.MALFORMED_NUMBER);

    if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
      throw new XmlParseException(ParseError.MALFORMED_NUMBER);

    substringBuilder.setEndExclusive(cursor.getNextCharIndex());
    String numberString = substringBuilder.build(false);

    if (encounteredDecimalPoint) {
      try {
        consumer.onDoubleAttribute(attributeName, Double.parseDouble(numberString));
      } catch (NumberFormatException e) {
        throw new XmlParseException(ParseError.MALFORMED_NUMBER);
      }
      return;
    }

    try {
      consumer.onLongAttribute(attributeName, Long.parseLong(numberString));
    } catch (NumberFormatException e) {
      throw new XmlParseException(ParseError.MALFORMED_NUMBER);
    }
  }

  private boolean tryParseAttribute() {
    cursor.consumeWhitespace();

    String attributeName = tryParseIdentifier(true);

    if (attributeName == null)
      return false;

    cursor.consumeWhitespace();

    if (cursor.nextChar() != '=')
      throw new XmlParseException(ParseError.MISSING_ATTRIBUTE_EQUALS);

    cursor.consumeWhitespace();

    switch (cursor.peekChar()) {
      case '"':
        consumer.onStringAttribute(attributeName, parseStringAttributeValue());
        return true;

      case '{':
        cursor.nextChar();

        long openingCurlyState = cursor.getState();

        consumer.onTagAttributeBegin(attributeName);

        parseInput(true);

        char nextChar = cursor.nextChar();

        if (nextChar != '}') {
          cursor.emitState(openingCurlyState);
          throw new XmlParseException(ParseError.UNTERMINATED_SUBTREE);
        }

        cursor.emitCurrentState();

        consumer.onTagAttributeEnd(attributeName);
        return true;

      case 'T':
      case 't':
        for (char c : TRUE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new XmlParseException(ParseError.MALFORMED_LITERAL_TRUE);
        }

        if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
          throw new XmlParseException(ParseError.MALFORMED_LITERAL_TRUE);

        consumer.onBooleanAttribute(attributeName, true);
        return true;

      case 'F':
      case 'f':
        for (char c : FALSE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new XmlParseException(ParseError.MALFORMED_LITERAL_FALSE);
        }

        if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
          throw new XmlParseException(ParseError.MALFORMED_LITERAL_FALSE);

        consumer.onBooleanAttribute(attributeName, false);
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
        throw new XmlParseException(ParseError.UNSUPPORTED_ATTRIBUTE_VALUE);
    }
  }

  private void parseOpeningOrClosingTag() {
    if (cursor.nextChar() != '<')
      throw new IllegalStateException("Expected an opening pointy-bracket!");

    long savedState = cursor.getState();

    cursor.consumeWhitespace();

    boolean wasClosingTag = false;

    if (cursor.peekChar() == '/') {
      cursor.nextChar();
      wasClosingTag = true;
    }

    String tagName = tryParseIdentifier(false);

    cursor.emitState(savedState);

    if (tagName == null)
      throw new XmlParseException(ParseError.MISSING_TAG_NAME);

    if (wasClosingTag) {
      if (cursor.nextChar() != '>')
        throw new XmlParseException(ParseError.UNTERMINATED_TAG);

      consumer.onTagClose(tagName);
      return;
    }

    consumer.onTagOpenBegin(tagName);

    while (cursor.hasRemainingChars()) {
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
      throw new XmlParseException(ParseError.UNTERMINATED_TAG);

    cursor.emitCurrentState();
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
