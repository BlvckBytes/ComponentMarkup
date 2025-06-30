package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import at.blvckbytes.component_markup.markup.xml.XmlParseException;

import java.util.ArrayList;
import java.util.List;

public class MarkupParseException extends RuntimeException implements ErrorMessage {

  public final CursorPosition position;
  public final MarkupParseError error;
  public final Object[] messagePlaceholders;

  public MarkupParseException(CursorPosition position, MarkupParseError error, Object... messagePlaceholders) {
    this.position = position;
    this.error = error;
    this.messagePlaceholders = messagePlaceholders;
  }

  public MarkupParseException(CursorPosition position, XmlParseException xmlException) {
    super(xmlException);

    this.position = position;
    this.error = MarkupParseError.XML_PARSE_ERROR;
    this.messagePlaceholders = new Object[0];
  }

  public MarkupParseException(CursorPosition position, ExpressionParseException expressionParseException) {
    super(expressionParseException);

    this.position = position;
    this.error = MarkupParseError.EXPRESSION_PARSE_ERROR;
    this.messagePlaceholders = new Object[0];
  }

  public MarkupParseException(CursorPosition position, ExpressionTokenizeException expressionTokenizeException) {
    super(expressionTokenizeException);

    this.position = position;
    this.error = MarkupParseError.EXPRESSION_TOKENIZE_ERROR;
    this.messagePlaceholders = new Object[0];
  }

  @Override
  public String getErrorMessage() {
    switch (this.error) {
      case XML_PARSE_ERROR:
        return ((XmlParseException) getCause()).getErrorMessage();

      case EXPRESSION_PARSE_ERROR:
        return ((ExpressionParseException) getCause()).getErrorMessage();

      case EXPRESSION_TOKENIZE_ERROR:
        return ((ExpressionTokenizeException) getCause()).getErrorMessage();

      default:
        return String.format(this.error.getErrorMessage(), messagePlaceholders);
    }
  }

  public int getCharIndex() {
    int charIndex = position.nextCharIndex == 0 ? 0 : position.nextCharIndex - 1;

    switch (this.error) {
      case EXPRESSION_PARSE_ERROR:
        charIndex += ((ExpressionParseException) getCause()).charIndex;
        break;

      case EXPRESSION_TOKENIZE_ERROR:
        charIndex += ((ExpressionTokenizeException) getCause()).beginIndex;
        break;
    }

    return charIndex;
  }

  public List<String> makeErrorScreen(String input) {
    List<String> result = new ArrayList<>();

    int inputLength = input.length();
    int targetCharIndex = getCharIndex();

    int lineCounter = 1;

    for (int index = 0; index < inputLength; ++index) {
      if (input.charAt(index) == '\n')
        ++lineCounter;
    }

    int maxLineNumberDigits = (lineCounter + 9) / 10;

    int nextLineNumber = 1;
    int lineBegin = 0;

    for (int index = 0; index < inputLength; ++index) {
      char currentChar = input.charAt(index);

      if (currentChar == '\r')
        continue;

      boolean isLastChar = index == inputLength - 1;

      if (currentChar == '\n' || isLastChar) {
        if (isLastChar)
          ++index;

        String lineNumber = String.format("%0" + maxLineNumberDigits + "d", nextLineNumber++) + ": ";
        String lineContents = input.substring(lineBegin, index);

        result.add(lineNumber + lineContents);

        if (targetCharIndex >= lineBegin && targetCharIndex < index) {
          int lineRelativeOffset = targetCharIndex - lineBegin;
          int charCountUntilTargetChar = lineRelativeOffset == 0 ? 0 : lineRelativeOffset + 1;
          int spacerLength = lineNumber.length() + charCountUntilTargetChar - 1;

          String spacer = makeIndent(spacerLength, '-');

          spacer += "^";

          result.add(spacer);
          result.add(makeIndent(lineNumber.length(), ' ') + "Error: " + getErrorMessage());
        }

        lineBegin = index + 1;
      }
    }

    return result;
  }

  private String makeIndent(int count, char c) {
    if (count <= 0)
      return "";

    StringBuilder result = new StringBuilder(count);

    for (int i = 0; i < count; ++i)
      result.append(c);

    return result.toString();
  }
}
