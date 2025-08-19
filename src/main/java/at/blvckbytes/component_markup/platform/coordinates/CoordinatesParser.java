/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.coordinates;

import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizer;
import at.blvckbytes.component_markup.expression.tokenizer.token.DoubleToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.LongToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoordinatesParser {

  public static @NotNull Coordinates parse(InputView input) {
    input.consumeWhitespace(null);

    Double xCoordinate = parseNumber(input);

    if (xCoordinate == null)
      throw new CoordinatesParseException(input, input.getPosition(), CoordinatesParseError.EXPECTED_X_COORDINATE);

    input.consumeWhitespace(null);

    Double yCoordinate = parseNumber(input);

    if (yCoordinate == null)
      throw new CoordinatesParseException(input, input.getPosition(), CoordinatesParseError.EXPECTED_Y_COORDINATE);

    input.consumeWhitespace(null);

    Double zCoordinate = parseNumber(input);

    if (zCoordinate == null)
      throw new CoordinatesParseException(input, input.getPosition(), CoordinatesParseError.EXPECTED_Z_COORDINATE);

    input.consumeWhitespace(null);

    InputView world = null;
    int worldBegin = input.getPosition() + 1;

    char currentChar;

    while ((currentChar = input.peekChar(0)) != 0) {
      if (currentChar == ' ')
        break;

      input.nextChar();
    }

    if (input.getPosition() >= worldBegin)
      world = input.buildSubViewAbsolute(worldBegin, input.getPosition() + 1);

    input.consumeWhitespace(null);

    if (input.peekChar(0) != 0)
      throw new CoordinatesParseException(input, input.getPosition() + 1, CoordinatesParseError.EXPECTED_END_AFTER_WORLD_NAME);

    return new Coordinates(xCoordinate, yCoordinate, zCoordinate, world);
  }

  private static @Nullable Double parseNumber(InputView input) {
    char upcomingChar = input.peekChar(0);
    boolean isNegative = false;

    int numberBegin;

    if (upcomingChar == '-') {
      isNegative = true;
      input.nextChar();
      numberBegin = input.getPosition();
      upcomingChar = input.peekChar(0);
    }

    else
      numberBegin = input.getPosition() + 1;

    boolean isDotDouble = false;

    if (!(upcomingChar >= '0' && upcomingChar <= '9' || (isDotDouble = upcomingChar == '.'))) {
      if (isNegative)
        throw new CoordinatesParseException(input, numberBegin, CoordinatesParseError.MALFORMED_NUMBER);

      return null;
    }

    ExpressionTokenizer expressionTokenizer = new ExpressionTokenizer(input, null);

    try {
      Token result;

      if (isDotDouble) {
        result = expressionTokenizer.tryParseDotDoubleToken();

        if (result == null)
          throw new IllegalStateException();
      }
      else
        result = expressionTokenizer.parseLongOrDoubleToken();

      double value;

      if (result instanceof LongToken)
        value = (double) ((LongToken) result).value;
      else
        value = ((DoubleToken) result).value;

      return value * (isNegative ? -1 : 1);
    } catch (Throwable e) {
      throw new CoordinatesParseException(input, numberBegin, CoordinatesParseError.MALFORMED_NUMBER);
    }
  }
}
