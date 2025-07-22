package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.SubstringBuilder;
import at.blvckbytes.component_markup.util.SubstringFlag;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class XmlEventParser {

  private static final EnumSet<SubstringFlag> SUBSTRING_AS_IS = EnumSet.noneOf(SubstringFlag.class);
  private static final EnumSet<SubstringFlag> SUBSTRING_INNER_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT);
  private static final EnumSet<SubstringFlag> SUBSTRING_FIRST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_LEADING_SPACE);
  private static final EnumSet<SubstringFlag> SUBSTRING_LAST_TEXT = EnumSet.of(SubstringFlag.REMOVE_NEWLINES_INDENT, SubstringFlag.REMOVE_TRAILING_SPACE);
  private static final EnumSet<SubstringFlag> SUBSTRING_ONLY_TEXT;

  static {
    SUBSTRING_ONLY_TEXT = EnumSet.allOf(SubstringFlag.class);
    SUBSTRING_ONLY_TEXT.remove(SubstringFlag.KEEP_REMOVE_INDICES);
  }

  private static final char[] TRUE_LITERAL_CHARS  = { 't', 'r', 'u', 'e' };
  private static final char[] FALSE_LITERAL_CHARS = { 'f', 'a', 'l', 's', 'e' };

  private final XmlEventConsumer consumer;
  private final TokenOutput tokenOutput;
  private final SubstringBuilder substringBuilder;
  private final InputCursor cursor;

  private XmlEventParser(String input, XmlEventConsumer consumer, @Nullable TokenOutput tokenOutput) {
    this.consumer = consumer;
    this.tokenOutput = tokenOutput;
    this.substringBuilder = new SubstringBuilder(input);
    this.cursor = new InputCursor(input, tokenOutput);
  }

  public static void parse(String input, XmlEventConsumer consumer) {
    new XmlEventParser(input, consumer, null).parseInput(false);
  }

  public static void parse(String input, XmlEventConsumer consumer, @Nullable TokenOutput tokenOutput) {
    new XmlEventParser(input, consumer, tokenOutput).parseInput(false);
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

        throw new XmlParseException(XmlParseError.UNESCAPED_CURLY);
      }

      CursorPosition preConsumePosition = cursor.getPosition();
      int possibleNonTextBeginIndex = cursor.getNextCharIndex();

      if (cursor.peekChar() == '{' && priorChar != '\\') {
        if (tokenOutput != null)
          tokenOutput.emitToken(cursor.getNextCharIndex(), TokenType.MARKUP__PUNCTUATION__INTERPOLATION, "{");

        cursor.nextChar();

        CursorPosition beginPosition = cursor.getPosition();

        if (substringBuilder.hasStartSet()) {
          substringBuilder.setEndExclusive(possibleNonTextBeginIndex);

          emitText(
            textContentBeginPosition,
            wasPriorTagOrInterpolation
              ? SUBSTRING_INNER_TEXT
              : SUBSTRING_FIRST_TEXT
          );
        }

        substringBuilder.setStartInclusive(cursor.getNextCharIndex());

        CursorPosition valueBeginPosition = null;

        while (cursor.peekChar() != 0) {
          int possibleTerminationIndex = cursor.getNextCharIndex();
          char currentChar = cursor.nextChar();

          if (currentChar == '\n' || currentChar == '{') {
            consumer.onCursorPosition(beginPosition);
            throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION);
          }

          if (valueBeginPosition == null)
            valueBeginPosition = cursor.getPosition();

          inStringDetector.onEncounter(currentChar);

          if (inStringDetector.isInString())
            continue;

          if (currentChar == '}') {
            if (tokenOutput != null)
              tokenOutput.emitToken(possibleTerminationIndex, TokenType.MARKUP__PUNCTUATION__INTERPOLATION, "}");

            substringBuilder.setEndExclusive(possibleTerminationIndex);
            break;
          }
        }

        consumer.onCursorPosition(beginPosition);

        if (!substringBuilder.hasEndSet())
          throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION);

        inStringDetector.reset();

        // The interpolation-expression will generate tokens within the expression-parser later on.

        String interpolationContents = substringBuilder.build(SUBSTRING_AS_IS);
        substringBuilder.resetIndices();

        consumer.onInterpolation(interpolationContents, valueBeginPosition);
        wasPriorTagOrInterpolation = true;
        continue;
      }

      CursorPosition firstSpacePosition = null;

      if (cursor.peekChar() == ' ') {
        if (tokenOutput != null)
          tokenOutput.emitToken(cursor.getNextCharIndex(), TokenType.ANY__WHITESPACE, " ");

        cursor.nextChar();
        firstSpacePosition = cursor.getPosition();
      }

      cursor.consumeWhitespace();

      if (cursor.peekChar() == '<') {
        if (substringBuilder.hasStartSet()) {
          substringBuilder.setEndExclusive(possibleNonTextBeginIndex);

          emitText(
            textContentBeginPosition,
            wasPriorTagOrInterpolation
              ? SUBSTRING_INNER_TEXT
              : SUBSTRING_FIRST_TEXT
          );
        }

        else if (wasPriorTagOrInterpolation && firstSpacePosition != null && firstSpacePosition.lineNumber == cursor.getLineNumber()) {
          substringBuilder.setStartInclusive(preConsumePosition.nextCharIndex);
          substringBuilder.setEndExclusive(cursor.getNextCharIndex());

          emitText(firstSpacePosition, SUBSTRING_AS_IS);
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

      if (nextChar == '\\' && (cursor.peekChar() == '<' || cursor.peekChar() == '}' || cursor.peekChar() == '{')) {
        substringBuilder.addIndexToBeRemoved(nextCharIndex);
        nextChar = cursor.nextChar();
      }

      priorChar = nextChar;

      cursor.consumeWhitespace();
    }

    if (substringBuilder.hasStartSet()) {
      substringBuilder.setEndExclusive(cursor.getNextCharIndex());

      emitText(
        textContentBeginPosition,
        wasPriorTagOrInterpolation
          ? SUBSTRING_LAST_TEXT
          : SUBSTRING_ONLY_TEXT
      );
    }

    if (!isWithinCurlyBrackets)
      consumer.onInputEnd();
  }

  private void emitText(CursorPosition position, EnumSet<SubstringFlag> flags) {
    String text = substringBuilder.build(flags);

    if (text.isEmpty()) {
      substringBuilder.resetIndices();
      return;
    }

    if (tokenOutput != null)
      tokenOutput.emitToken(position.nextCharIndex - 1, TokenType.MARKUP__PLAIN_TEXT, substringBuilder.build(SUBSTRING_AS_IS));

    substringBuilder.resetIndices();

    consumer.onCursorPosition(position);
    consumer.onText(text);
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

    String identifier = substringBuilder.build(SUBSTRING_AS_IS);

    substringBuilder.resetIndices();

    return identifier;
  }

  private void parseAndEmitStringAttributeValue(String attributeName) {
    int beginIndex = cursor.getNextCharIndex();

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

    String value = substringBuilder.build(SUBSTRING_AS_IS);

    substringBuilder.resetIndices();

    if (tokenOutput != null)
      tokenOutput.emitToken(beginIndex, TokenType.MARKUP__STRING, '"' + value + '"');

    consumer.onStringAttribute(attributeName, valueBeginPosition, value);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean doesEndOrHasTrailingWhiteSpaceOrTagTermination() {
    if (cursor.peekChar() == 0)
      return true;

    char peekedChar = cursor.peekChar();

    if (Character.isWhitespace(peekedChar))
      return true;

    return peekedChar == '>';
  }

  private void parseNumericAttributeValue(String attributeName) {
    int numberBeginIndex = cursor.getNextCharIndex();
    substringBuilder.setStartInclusive(numberBeginIndex);

    if (cursor.peekChar() == '-')
      cursor.nextChar();

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

    int numberEndIndex = cursor.getNextCharIndex();

    substringBuilder.setEndExclusive(numberEndIndex);
    String numberString = substringBuilder.build(SUBSTRING_AS_IS);

    if (tokenOutput != null)
      tokenOutput.emitToken(numberBeginIndex, TokenType.MARKUP__NUMBER, numberString);

    substringBuilder.resetIndices();

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

    // Attribute-name tokens are emitted by the markup-parser, which knows more about the details
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

    if (tokenOutput != null)
      tokenOutput.emitToken(cursor.getNextCharIndex(), TokenType.MARKUP__PUNCTUATION__EQUALS, "=");

    cursor.nextChar();

    cursor.consumeWhitespace();

    switch (cursor.peekChar()) {
      case '"':
        parseAndEmitStringAttributeValue(attributeName);
        return true;

      case '{':
        if (tokenOutput != null)
          tokenOutput.emitToken(cursor.getNextCharIndex(), TokenType.MARKUP__PUNCTUATION__SUBTREE, "{");

        cursor.nextChar();

        CursorPosition openingCurlyPosition = cursor.getPosition();

        consumer.onTagAttributeBegin(attributeName);

        parseInput(true);

        char nextChar = cursor.nextChar();

        if (nextChar != '}') {
          consumer.onCursorPosition(openingCurlyPosition);
          throw new XmlParseException(XmlParseError.UNTERMINATED_MARKUP_VALUE);
        }

        if (tokenOutput != null)
          tokenOutput.emitToken(cursor.getNextCharIndex() - 1, TokenType.MARKUP__PUNCTUATION__SUBTREE, "}");

        consumer.onCursorPosition(cursor.getPosition());
        consumer.onTagAttributeEnd(attributeName);
        return true;

      case 'T':
      case 't': {
        int beginIndex = cursor.getNextCharIndex();

        for (char c : TRUE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_TRUE);
        }

        if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
          throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_TRUE);

        if (tokenOutput != null)
          tokenOutput.emitToken(beginIndex, TokenType.MARKUP__LITERAL__ANY, "true");

        consumer.onBooleanAttribute(attributeName, "true", true);
        return true;
      }

      case 'F':
      case 'f':
        int beginIndex = cursor.getNextCharIndex();

        for (char c : FALSE_LITERAL_CHARS) {
          if (Character.toLowerCase(cursor.nextChar()) != c)
            throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_FALSE);
        }

        if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
          throw new XmlParseException(XmlParseError.MALFORMED_LITERAL_FALSE);

        if (tokenOutput != null)
          tokenOutput.emitToken(beginIndex, TokenType.MARKUP__LITERAL__ANY, "false");

        consumer.onBooleanAttribute(attributeName, "false", false);
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
    int openingIndex = cursor.getNextCharIndex();

    if (cursor.nextChar() != '<')
      throw new IllegalStateException("Expected an opening pointy-bracket!");

    if (tokenOutput != null)
      tokenOutput.emitToken(openingIndex, TokenType.MARKUP__PUNCTUATION__TAG, "<");

    CursorPosition savedPosition = cursor.getPosition();

    cursor.consumeWhitespace();

    boolean wasClosingTag = false;

    if (cursor.peekChar() == '/') {
      if (tokenOutput != null)
        tokenOutput.emitToken(cursor.getNextCharIndex(), TokenType.MARKUP__PUNCTUATION__TAG, "/");

      cursor.nextChar();
      wasClosingTag = true;
    }

    cursor.consumeWhitespace();

    int tagNameIndex = cursor.getNextCharIndex();
    String tagName = tryParseIdentifier(false);

    if (wasClosingTag) {
      consumer.onCursorPosition(savedPosition);

      if (cursor.nextChar() != '>')
        throw new XmlParseException(XmlParseError.UNTERMINATED_TAG);

      if (tokenOutput != null) {
        tokenOutput.emitToken(tagNameIndex, TokenType.MARKUP__IDENTIFIER__TAG, tagName);
        tokenOutput.emitToken(cursor.getNextCharIndex() - 1, TokenType.MARKUP__PUNCTUATION__TAG, ">");
      }

      consumer.onTagClose(tagName);
      return;
    }

    if (tagName == null) {
      consumer.onCursorPosition(savedPosition);
      throw new XmlParseException(XmlParseError.MISSING_TAG_NAME);
    }

    if (tryConsumeCommentTag(tagName)) {
      if (tokenOutput != null)
        tokenOutput.emitToken(openingIndex, TokenType.MARKUP__COMMENT, cursor.input.substring(openingIndex, cursor.getNextCharIndex()));

      return;
    }

    if (tokenOutput != null)
      tokenOutput.emitToken(tagNameIndex, TokenType.MARKUP__IDENTIFIER__TAG, tagName);

    consumer.onCursorPosition(savedPosition);
    consumer.onTagOpenBegin(tagName);

    while (cursor.peekChar() != 0) {
      if (!tryParseAttribute())
        break;
    }

    boolean wasSelfClosing = false;

    if (cursor.peekChar() == '/') {
      if (tokenOutput != null)
        tokenOutput.emitToken(cursor.getNextCharIndex(), TokenType.MARKUP__PUNCTUATION__TAG, "/");

      wasSelfClosing = true;
      cursor.nextChar();
    }

    cursor.consumeWhitespace();

    if (cursor.nextChar() != '>')
      throw new XmlParseException(XmlParseError.UNTERMINATED_TAG);

    if (tokenOutput != null)
      tokenOutput.emitToken(cursor.getNextCharIndex() - 1, TokenType.MARKUP__PUNCTUATION__TAG, ">");

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
