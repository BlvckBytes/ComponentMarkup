/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.token.TokenOutput;
import at.blvckbytes.component_markup.markup.parser.token.TokenType;
import at.blvckbytes.component_markup.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class XmlEventParser {

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

  private void parseInput(boolean isWithinCurlyBrackets) {
    boolean wasPriorTagOrInterpolation = false;
    int textStartInclusive = -1;

    char upcomingChar;

    while ((upcomingChar = input.peekChar(0)) != 0) {
      if (upcomingChar == '}' && input.priorChar(0) != '\\') {
        if (isWithinCurlyBrackets)
          break;

        input.nextChar();

        throw new XmlParseException(XmlParseError.UNESCAPED_CURLY, input.getPosition());
      }

      if (upcomingChar == '{' && input.priorChar(0) != '\\') {
        if (textStartInclusive != -1) {
          emitText(
            input.buildSubViewAbsolute(textStartInclusive, input.getPosition() + 1),
            wasPriorTagOrInterpolation
              ? SubstringFlag.INNER_TEXT
              : SubstringFlag.FIRST_TEXT
          );
          textStartInclusive = -1;
        }

        input.nextChar();

        int startInclusive = input.getPosition();

        ExpressionNode interpolationExpression;

        try {
          interpolationExpression = ExpressionParser.parse(input, tokenOutput);
        } catch (ExpressionTokenizeException expressionTokenizeException) {
          throw new MarkupParseException(startInclusive, expressionTokenizeException);
        } catch (ExpressionParseException expressionParseException) {
          throw new MarkupParseException(startInclusive, expressionParseException);
        }

        input.consumeWhitespace(tokenOutput);

        if (input.nextChar() != '}')
          throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION, startInclusive);

        if (interpolationExpression == null) {
          if (input.nextChar() != '}')
            throw new XmlParseException(XmlParseError.UNTERMINATED_INTERPOLATION, startInclusive);

          // TODO: Proper error; add test-case
          throw new IllegalStateException("Expected interpolation-expression after opening {");
        }

        StringView rawInterpolation = input.buildSubViewRelative(startInclusive, input.getPosition() + 1);

        if (tokenOutput != null)
          tokenOutput.emitToken(TokenType.MARKUP__INTERPOLATION, rawInterpolation);

        consumer.onInterpolation(interpolationExpression, rawInterpolation);

        wasPriorTagOrInterpolation = true;
        continue;
      }

      if (upcomingChar == '<') {
        if (textStartInclusive != -1) {
          emitText(
            input.buildSubViewAbsolute(textStartInclusive, input.getPosition() + 1),
            wasPriorTagOrInterpolation
              ? SubstringFlag.INNER_TEXT
              : SubstringFlag.FIRST_TEXT
          );
          textStartInclusive = -1;
        }

        parseOpeningOrClosingTag();
        wasPriorTagOrInterpolation = true;
        continue;
      }

      char currentChar = input.nextChar();

      if (Character.isWhitespace(currentChar) && tokenOutput != null)
        tokenOutput.emitCharToken(input.getPosition(), TokenType.ANY__WHITESPACE);

      if (textStartInclusive == -1) {
        if (currentChar == '\n') {
          input.consumeWhitespace(tokenOutput);
          continue;
        }

        textStartInclusive = input.getPosition();
      }

      if (currentChar == '\\' && ((upcomingChar = input.peekChar(0)) == '<' || upcomingChar == '}' || upcomingChar == '{')) {
        input.addIndexToBeRemoved(input.getPosition());
        input.nextChar();
      }

      input.consumeWhitespace(tokenOutput);
    }

    if (textStartInclusive != -1) {
      emitText(
        input.buildSubViewAbsolute(textStartInclusive, input.getPosition() + 1),
        wasPriorTagOrInterpolation
          ? SubstringFlag.LAST_TEXT
          : SubstringFlag.ONLY_TEXT
      );
    }

    if (!isWithinCurlyBrackets)
      consumer.onInputEnd();
  }

  private void emitText(StringView text, @Nullable EnumSet<SubstringFlag> flags) {
    if (flags != null)
      text.setBuildFlags(flags);

    // This check is taking care of various special cases, where if the parser-loop would have
    // to detect that we shouldn't emit certain whitespace, needless complexity would be added.
    if (text.buildString().isEmpty())
      return;

    consumer.onText(text);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__PLAIN_TEXT, text);
  }

  private @Nullable StringView tryParseIdentifier() {
    if (!isIdentifierChar(input.peekChar(0)))
      return null;

    input.nextChar();

    int startInclusive = input.getPosition();

    while (isIdentifierChar(input.peekChar(0)))
      input.nextChar();

    return input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);
  }

  private void parseAndEmitStringAttributeValue(StringView attributeName) {
    if (input.nextChar() != '"')
      throw new IllegalStateException("Expected opening double-quotes");

    int startInclusive = input.getPosition();
    int endInclusive = -1;

    char currentChar;

    while ((currentChar = input.nextChar()) != 0) {
      if (currentChar == '\r' || currentChar == '\n')
        throw new XmlParseException(XmlParseError.UNTERMINATED_STRING, startInclusive);

      if (currentChar == '"') {
        if (input.priorChar(1) == '\\') {
          input.addIndexToBeRemoved(input.getPosition() - 1);
        } else {
          endInclusive = input.getPosition();
          break;
        }
      }

      if (tokenOutput != null && Character.isWhitespace(currentChar))
        tokenOutput.emitCharToken(input.getPosition(), TokenType.ANY__WHITESPACE);
    }

    if (endInclusive < 0)
      throw new XmlParseException(XmlParseError.UNTERMINATED_STRING, startInclusive);

    StringView stringValue = input.buildSubViewAbsolute(startInclusive + 1, endInclusive);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__STRING, input.buildSubViewAbsolute(startInclusive, endInclusive + 1));

    consumer.onStringAttribute(attributeName, stringValue);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean doesEndOrHasTrailingWhiteSpaceOrTagTermination() {
    char peekedChar = input.peekChar(0);

    if (peekedChar == 0)
      return true;

    if (Character.isWhitespace(peekedChar))
      return true;

    return peekedChar == '>' || (peekedChar == '/' && input.peekChar(1) == '>');
  }

  private void parseNumericAttributeValue(StringView attributeName) {
    int startInclusive = -1;

    if (input.peekChar(0) == '-') {
      input.nextChar();
      startInclusive = input.getPosition();
    }

    boolean encounteredDecimalPoint = false;
    boolean encounteredDigit = false;

    char peekedChar;

    while ((peekedChar = input.peekChar(0)) != 0) {
      if (peekedChar >= '0' && peekedChar <= '9')
        encounteredDigit = true;

      else if (peekedChar == '.') {
        if (encounteredDecimalPoint)
          throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, startInclusive);

        encounteredDecimalPoint = true;
      }

      else
        break;

      input.nextChar();

      if (startInclusive < 0)
        startInclusive = input.getPosition();
    }

    if (startInclusive < 0)
      throw new IllegalStateException("Expected to be called only if peekChar(0) in [0-9\\-.]");

    if (!encounteredDigit)
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, startInclusive);

    if (!doesEndOrHasTrailingWhiteSpaceOrTagTermination())
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, startInclusive);

    StringView value = input.buildSubViewAbsolute(startInclusive, input.getPosition() + 1);

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__NUMBER, value);

    if (encounteredDecimalPoint) {
      try {
        consumer.onDoubleAttribute(attributeName, value, Double.parseDouble(value.buildString()));
      } catch (NumberFormatException e) {
        throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, startInclusive);
      }
      return;
    }

    try {
      consumer.onLongAttribute(attributeName, value, Long.parseLong(value.buildString()));
    } catch (NumberFormatException e) {
      throw new XmlParseException(XmlParseError.MALFORMED_NUMBER, startInclusive);
    }
  }

  private boolean tryParseAttribute() {
    input.consumeWhitespace(tokenOutput);

    // Attribute-name tokens are emitted by the markup-parser, which knows more about the details
    StringView attributeName = tryParseIdentifier();

    if (attributeName == null) {
      if (input.peekChar(0) == '"') {
        input.nextChar();
        throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY, input.getPosition());
      }

      return false;
    }

    // Attribute-identifiers do not support leading digits, as this constraint allows for
    // proper detection of malformed input; example: my-attr 53 (missing an equals-sign).
    if (Character.isDigit(attributeName.nthChar(0)))
      throw new XmlParseException(XmlParseError.EXPECTED_ATTRIBUTE_KEY, attributeName.startInclusive);

    input.consumeWhitespace(tokenOutput);

    if (input.peekChar(0) != '=') {
      consumer.onFlagAttribute(attributeName);
      return true;
    }

    input.nextChar();

    if (tokenOutput != null)
      tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__EQUALS);

    input.consumeWhitespace(tokenOutput);

    switch (input.peekChar(0)) {
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
        throw new XmlParseException(XmlParseError.UNSUPPORTED_ATTRIBUTE_VALUE, input.getPosition() + 1);
    }
  }

  private boolean tryConsumeAndEmitCommentTag(int openingPosition) {
    if (input.peekChar(0) != '!' || input.peekChar(1) != '-' || input.peekChar(2) != '-')
      return false;

    char currentChar;

    while ((currentChar = input.nextChar()) != 0) {
      if (currentChar == '<' && tryConsumeAndEmitCommentTag(input.getPosition()))
        continue;

      if (currentChar == '-' && input.nextChar() == '-' && input.nextChar() == '>') {
        if (tokenOutput != null) {
          tokenOutput.emitToken(
            TokenType.MARKUP__COMMENT,
            input.buildSubViewAbsolute(openingPosition, input.getPosition() + 1)
          );
        }

        return true;
      }
    }

    throw new XmlParseException(XmlParseError.MALFORMED_COMMENT, openingPosition);
  }

  private void parseOpeningOrClosingTag() {
    if (input.nextChar() != '<')
      throw new IllegalStateException("Expected an opening pointy-bracket!");

    int openingPosition = input.getPosition();

    if (tryConsumeAndEmitCommentTag(openingPosition))
      return;

    if (tokenOutput != null)
      tokenOutput.emitCharToken(openingPosition, TokenType.MARKUP__PUNCTUATION__TAG);

    input.consumeWhitespace(tokenOutput);

    boolean wasClosingTag = false;

    if (input.peekChar(0) == '/') {
      input.nextChar();

      if (tokenOutput != null)
        tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__TAG);

      wasClosingTag = true;
    }

    input.consumeWhitespace(tokenOutput);

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

    if (tokenOutput != null)
      tokenOutput.emitToken(TokenType.MARKUP__IDENTIFIER__TAG, tagName);

    consumer.onTagOpenBegin(tagName);

    while (input.peekChar(0) != 0) {
      if (!tryParseAttribute())
        break;
    }

    boolean wasSelfClosing = false;

    if (input.peekChar(0) == '/') {
      input.nextChar();
      wasSelfClosing = true;

      if (tokenOutput != null)
        tokenOutput.emitCharToken(input.getPosition(), TokenType.MARKUP__PUNCTUATION__TAG);
    }

    input.consumeWhitespace(tokenOutput);

    if (input.nextChar() != '>')
      throw new XmlParseException(XmlParseError.UNTERMINATED_TAG, openingPosition);

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
