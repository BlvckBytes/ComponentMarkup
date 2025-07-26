package at.blvckbytes.component_markup.markup.parser;

import at.blvckbytes.component_markup.ErrorMessage;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.markup.xml.XmlParseException;
import at.blvckbytes.component_markup.util.StringPosition;

import java.util.ArrayList;
import java.util.List;

public class MarkupParseException extends RuntimeException implements ErrorMessage {

  public final StringPosition position;
  public final MarkupParseError error;
  public final String[] messagePlaceholders;

  public MarkupParseException(StringPosition position, MarkupParseError error, String... messagePlaceholders) {
    this.position = position;
    this.error = error;
    this.messagePlaceholders = messagePlaceholders;
  }

  public MarkupParseException(StringPosition position, XmlParseException xmlException) {
    super(xmlException);

    this.position = position;
    this.error = MarkupParseError.XML_PARSE_ERROR;
    this.messagePlaceholders = new String[0];
  }

  public MarkupParseException(StringPosition position, ExpressionParseException expressionParseException) {
    super(expressionParseException);

    this.position = position;
    this.error = MarkupParseError.EXPRESSION_PARSE_ERROR;
    this.messagePlaceholders = new String[0];
  }

  public MarkupParseException(StringPosition position, ExpressionTokenizeException expressionTokenizeException) {
    super(expressionTokenizeException);

    this.position = position;
    this.error = MarkupParseError.EXPRESSION_TOKENIZE_ERROR;
    this.messagePlaceholders = new String[0];
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
        return this.error.messageBuilder.apply(messagePlaceholders);
    }
  }

  public int getCharIndex() {
    int charIndex = position.charIndex;

    switch (this.error) {
      case EXPRESSION_PARSE_ERROR:
        charIndex += ((ExpressionParseException) getCause()).position.charIndex;
        break;

      case EXPRESSION_TOKENIZE_ERROR:
        charIndex += ((ExpressionTokenizeException) getCause()).position.charIndex;
        break;
    }

    return charIndex;
  }

  public List<String> makeErrorScreen() {
    List<String> result = new ArrayList<>();

    String input = position.rootView.contents;

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

        String lineNumber = padLeft(nextLineNumber++, maxLineNumberDigits) + ": ";
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

  private String padLeft(int number, int width) {
    String numberString = Integer.toString(number);

    int pad = width - numberString.length();

    if (pad <= 0)
      return numberString;

    StringBuilder result = new StringBuilder(width);

    for (int i = 0; i < pad; i++)
      result.append('0');

    result.append(numberString);

    return result.toString();
  }
}
