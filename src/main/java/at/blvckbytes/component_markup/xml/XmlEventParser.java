package at.blvckbytes.component_markup.xml;

import org.jetbrains.annotations.Nullable;

public class XmlEventParser {

  private static final char[] TRUE_LITERAL_CHARS  = { 't', 'r', 'u', 'e' };
  private static final char[] FALSE_LITERAL_CHARS = { 'f', 'a', 'l', 's', 'e' };
  private static final char[] NULL_LITERAL_CHARS  = { 'n', 'u', 'l', 'l' };

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

  private void parseInput(boolean isWithinCurlyBrackets) {
    char priorChar = 0;
    long textContentBeginState = -1;

    while (cursor.hasRemainingChars()) {
      if (isWithinCurlyBrackets) {
        if (cursor.peekChar() == '}' && priorChar != '\\')
          break;
      }

      int possibleTagBeginIndex = cursor.getNextCharIndex();
      long preConsumeWhitespaceState = cursor.getState();

      cursor.consumeWhitespace();

      if (cursor.peekChar() == '<') {
        if (substringBuilder.hasStartSet()) {
          substringBuilder.setEndExclusive(possibleTagBeginIndex);
          cursor.applyState(textContentBeginState, false, true);
          consumer.onText(substringBuilder.build(true));
        }

        parseOpeningOrClosingTag();
        priorChar = 0;
        continue;
      }

      cursor.applyState(preConsumeWhitespaceState, true, false);

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
        cursor.applyState(textContentBeginState, false, true);
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
      throw new IllegalStateException("Encountered unterminated string");

    return substringBuilder.build(false);
  }

  private boolean tryParseNumericAttributeValue(String attributeName) {
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
          throw new IllegalStateException("Numeric value contained multiple decimal-points");

        cursor.nextChar();
        encounteredDecimalPoint = true;
        continue;
      }

      break;
    }

    if (!encounteredDigit)
      throw new IllegalStateException("A numeric value must contain at least one digit");

    substringBuilder.setEndExclusive(cursor.getNextCharIndex());
    String numberString = substringBuilder.build(false);

    if (encounteredDecimalPoint) {
      try {
        consumer.onDoubleAttribute(attributeName, Double.parseDouble(numberString));
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }

    try {
      consumer.onLongAttribute(attributeName, Long.parseLong(numberString));
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean tryParseAttribute() {
    cursor.consumeWhitespace();

    String attributeName = tryParseIdentifier(true);

    if (attributeName == null)
      return false;

    cursor.consumeWhitespace();

    if (cursor.nextChar() != '=')
      throw new IllegalStateException("Expected equals-sign to pair key and value of an attribute");

    cursor.consumeWhitespace();

    switch (cursor.peekChar()) {
      case '"':
        consumer.onStringAttribute(attributeName, parseStringAttributeValue());
        return true;

      case '{':
        cursor.nextChar();

        consumer.onTagAttributeBegin(attributeName);

        parseInput(true);

        if (cursor.nextChar() != '}')
          throw new IllegalStateException("Expected closing }");

        cursor.emitCurrentState();
        consumer.onTagAttributeEnd(attributeName);

        return true;

      case 'T':
      case 't':
        for (char c : TRUE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new IllegalStateException("Expected true literal value");
        }

        consumer.onBooleanAttribute(attributeName, true);
        return true;

      case 'F':
      case 'f':
        for (char c : FALSE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new IllegalStateException("Expected false literal value");
        }

        consumer.onBooleanAttribute(attributeName, false);
        return true;

      case 'N':
      case 'n':
        for (char c : NULL_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new IllegalStateException("Expected null literal value");
        }

        consumer.onNullAttribute(attributeName);
        return true;

      default:
        if (!tryParseNumericAttributeValue(attributeName))
          throw new IllegalStateException("Expected numeric attribute value");

        return true;
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

    if (tagName == null)
      throw new IllegalStateException("Expected tag-name");

    cursor.applyState(savedState, false, true);

    if (wasClosingTag) {
      if (cursor.nextChar() != '>')
        throw new IllegalStateException("Expected tag-close char '>'");

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
      throw new IllegalStateException("Expected tag-close char '>'");

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
