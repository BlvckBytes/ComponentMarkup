/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizeException;
import at.blvckbytes.component_markup.expression.tokenizer.ExpressionTokenizer;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.platform.selector.argument.*;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SelectorParser {

  private static final ArgumentValue RANGE_VALUE_SENTINEL = () -> false;

  public static @NotNull TargetSelector parse(InputView input) {
    input.consumeWhitespace(null);

    if (input.nextChar() != '@')
      throw new SelectorParseException(input, SelectorParseError.MISSING_AT_SYMBOL);

    char peekedChar;

    if ((peekedChar = input.peekChar(0)) == 0 || Character.isWhitespace(peekedChar))
      throw new SelectorParseException(input, SelectorParseError.MISSING_TARGET_TYPE);

    input.nextChar();

    int typeBegin = input.getPosition();

    char upcomingChar;

    while ((upcomingChar = input.peekChar(0)) != 0 && upcomingChar != '[' && !Character.isWhitespace(upcomingChar))
      input.nextChar();

    InputView rawTarget = input.buildSubViewAbsolute(typeBegin, input.getPosition() + 1).setLowercase();
    TargetType target = TargetType.ofName(rawTarget);

    if (target == null)
      throw new SelectorParseException(input, typeBegin, SelectorParseError.UNKNOWN_TARGET_TYPE, rawTarget.buildString());

    input.consumeWhitespace(null);

    if (input.peekChar(0) == 0)
      return new TargetSelector(target, rawTarget, Collections.emptyList());

    if (input.nextChar() != '[')
      throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENTS_OPENING_BRACKET);

    List<ArgumentEntry> arguments = parseArguments(input);

    if (input.nextChar() != ']')
      throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENTS_CLOSING_BRACKET);

    return new TargetSelector(target, rawTarget, arguments);
  }

  private static List<ArgumentEntry> parseArguments(InputView input) {
    List<ArgumentEntry> result = new ArrayList<>();

    while (input.peekChar(0) != 0) {
      input.consumeWhitespace(null);

      char firstNameChar = input.peekChar(0);

      if (firstNameChar == 0 || firstNameChar == ']')
        return result;

      input.nextChar();

      int nameBegin = input.getPosition();

      if (firstNameChar == '=')
        throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENT_NAME);

      if (firstNameChar == ',')
        throw new SelectorParseException(input, SelectorParseError.DANGLING_ARGUMENT_SEPARATOR);

      char peekedChar;

      while ((peekedChar = input.peekChar(0)) >= 'a' && peekedChar <= 'z' || peekedChar >= 'A' && peekedChar <= 'Z')
        input.nextChar();

      InputView rawName = input.buildSubViewAbsolute(nameBegin, input.getPosition() + 1).setLowercase();
      ArgumentName name = ArgumentName.ofName(rawName);

      if (name == null)
        throw new SelectorParseException(input, nameBegin, SelectorParseError.UNKNOWN_ARGUMENT_NAME, rawName.buildString());

      input.consumeWhitespace(null);

      if (input.peekChar(0) != '=')
        throw new SelectorParseException(input, SelectorParseError.MISSING_EQUALS_SIGN, rawName.buildString());

      input.nextChar();
      input.consumeWhitespace(null);

      ArgumentValue value = parseArgumentValue(input, name, nameBegin);

      if (name.typeErrorProvider != null) {
        SelectorParseError typeError = name.typeErrorProvider.apply(value);

        if (typeError != null)
          throw new SelectorParseException(input, nameBegin, typeError, name.name);
      }

      for (ArgumentEntry entry : result) {
        if (entry.name != name)
          continue;

        switch (name.multiAllowance) {
          case NEVER:
            throw new SelectorParseException(input, nameBegin, SelectorParseError.MULTI_NEVER_ARGUMENT, name.name);

          case MULTI_IF_EITHER:
            continue;

          case MULTI_IF_NEGATED:
            if (!value.isNegated())
              throw new SelectorParseException(input, nameBegin, SelectorParseError.MULTI_IF_NEGATED_ARGUMENT, name.name);
            break;

          default:
            LoggerProvider.log(Level.WARNING, "Unaccounted-for multi-allowance: " + name.multiAllowance);
        }
      }

      result.add(new ArgumentEntry(name, rawName, value));

      input.consumeWhitespace(null);

      char upcomingChar = input.peekChar(0);

      if (upcomingChar == ']')
        return result;

      if (upcomingChar != ',')
        throw new SelectorParseException(input, SelectorParseError.MISSING_ARGUMENT_SEPARATOR, rawName.buildString());

      input.nextChar();
    }

    return result;
  }

  private static ArgumentValue parseArgumentValue(InputView input, ArgumentName name, int nameBegin) {
    char firstChar = input.peekChar(0);

    if (firstChar == '"' || firstChar == '!' && input.peekChar(1) == '"') {
      try {
        boolean isNegated = false;

        if (firstChar == '!') {
          isNegated = true;
          input.nextChar();
        }

        TerminalToken token = new ExpressionTokenizer(input, null).parseStringToken();
        return new StringValue(token.raw, (String) token.getPlainValue(), isNegated);
      } catch (ExpressionTokenizeException e) {
        // That's the only reason as to why this tokenizer-method would throw
        throw new SelectorParseException(input, e.position, SelectorParseError.UNTERMINATED_STRING);
      }
    }

    if (name.acceptedValue == AcceptedValue.STRING || name.acceptedValue == AcceptedValue.SORT_CRITERION) {
      boolean isNegated = false;

      if (firstChar == '!') {
        isNegated = true;
        input.nextChar();
      }

      int startIndex = input.getPosition() + 1;

      char upcomingChar;

      while ((upcomingChar = input.peekChar(0)) != 0) {
        if (Character.isWhitespace(upcomingChar))
          break;

        if (upcomingChar == ',' || upcomingChar == ']')
          break;

        char priorChar = input.priorChar(0);

        if (input.nextChar() == '"') {
          if (priorChar == '\\') {
            input.addIndexToBeRemoved(input.getPosition() - 1);
            continue;
          }

          throw new SelectorParseException(input, SelectorParseError.STRING_CONTAINED_QUOTE);
        }
      }

      InputView contents = input.buildSubViewAbsolute(startIndex, input.getPosition() + 1);

      if (name.acceptedValue == AcceptedValue.SORT_CRITERION) {
        SortCriterion sortCriterion = SortCriterion.ofName(contents);

        if (sortCriterion != null) {
          if (isNegated)
            throw new SelectorParseException(input, nameBegin, SelectorParseError.VALIDATION_FAILED_IS_NEGATED, name.name);

          return sortCriterion;
        }
      }

      return new StringValue(contents, contents.buildString(), isNegated);
    }

    int inputBegin = input.getPosition() + 1;

    NumericValue firstNumber;
    ArgumentValue currentValue = parseNumberOrRangeOperator(input);

    input.consumeWhitespace(null);

    if (currentValue == null) {
      if (name.acceptedValue == AcceptedValue.RANGE)
        throw new SelectorParseException(input, nameBegin, SelectorParseError.VALIDATION_FAILED_IS_NON_NUMERIC_OR_RANGE, name.name);

      throw new SelectorParseException(input, nameBegin, SelectorParseError.VALIDATION_FAILED_IS_NON_NUMERIC, name.name);
    }

    if (currentValue instanceof NumericValue) {
      char upcomingChar = input.peekChar(0);

      if (upcomingChar != '.' && upcomingChar != '-' && !(upcomingChar >= '0' && upcomingChar <= '9'))
        return currentValue;

      firstNumber = (NumericValue) currentValue;
    }

    // value is range-operator
    else {
      currentValue = parseNumberOrRangeOperator(input);

      if (currentValue == null)
        throw new SelectorParseException(input, inputBegin, SelectorParseError.EXPECTED_RHS_OF_RANGE);

      if (currentValue instanceof NumericValue)
        return validateRange(new NumericRangeValue(null, (NumericValue) currentValue));

      throw new SelectorParseException(input, inputBegin, SelectorParseError.DOUBLE_RANGE_OPERATOR);
    }

    currentValue = parseNumberOrRangeOperator(input);

    input.consumeWhitespace(null);

    if (currentValue != RANGE_VALUE_SENTINEL)
      throw new SelectorParseException(input, inputBegin, SelectorParseError.EXPECTED_RANGE_OPERATOR);

    currentValue = parseNumberOrRangeOperator(input);

    if (currentValue == null)
      return validateRange(new NumericRangeValue(firstNumber, null));

    if (currentValue == RANGE_VALUE_SENTINEL)
      throw new SelectorParseException(input, inputBegin, SelectorParseError.DOUBLE_RANGE_OPERATOR);

    return validateRange(new NumericRangeValue(firstNumber, (NumericValue) currentValue));
  }

  private static boolean isUpcomingCharInvalidNumberTermination(InputView input) {
    char upcomingChar = input.peekChar(0);

    if (upcomingChar == 0)
      return false;

    if (upcomingChar == '.')
      return input.peekChar(1) != '.';

    return upcomingChar != ',' && upcomingChar != ']' && !Character.isWhitespace(upcomingChar);
  }

  private static @Nullable ArgumentValue parseNumberOrRangeOperator(InputView input) {
    ExpressionTokenizer expressionTokenizer = new ExpressionTokenizer(input, null);

    char upcomingChar = input.peekChar(0);
    int valueBeginPosition = -1;

    boolean isNegated = false;
    int negationPosition = -1;

    if (upcomingChar == '!') {
      isNegated = true;
      input.nextChar();
      upcomingChar = input.peekChar(0);
      negationPosition = valueBeginPosition = input.getPosition();
    }

    boolean isNegative = false;
    int negativePosition = -1;

    if (upcomingChar == '-') {
      isNegative = true;
      input.nextChar();
      upcomingChar = input.peekChar(0);
      negativePosition = input.getPosition();

      if (valueBeginPosition < 0)
        valueBeginPosition = negativePosition;
    }

    if (valueBeginPosition < 0)
      valueBeginPosition = input.getPosition() + 1;

    InputView rawValue = null;
    Number value = null;

    if (upcomingChar >= '0' && upcomingChar <= '9') {
      try {
        Token result = expressionTokenizer.parseLongOrDoubleToken();

        if (result instanceof LongToken) {
          value = ((LongToken) result).value;
          rawValue = result.raw;
        } else if (result instanceof DoubleToken) {
          value = ((DoubleToken) result).value;
          rawValue = result.raw;
        }
      } catch (ExpressionTokenizeException ignored) {}

      if (value == null || isUpcomingCharInvalidNumberTermination (input))
        throw new SelectorParseException(input, valueBeginPosition, SelectorParseError.MALFORMED_NUMBER);
    }

    else if (upcomingChar == '.') {
      if (input.peekChar(1) == '.') {
        input.nextChar();
        input.nextChar();

        if (isNegative)
          throw new SelectorParseException(input, negativePosition, SelectorParseError.DANGLING_MINUS_SIGN);

        if (isNegated)
          throw new SelectorParseException(input, negationPosition, SelectorParseError.DANGLING_BANG);

        return RANGE_VALUE_SENTINEL;
      }

      try {
        Token result = expressionTokenizer.tryParseDotDoubleToken();

        if (result instanceof DoubleToken) {
          value = ((DoubleToken) result).value;
          rawValue = result.raw;
        }
      } catch (ExpressionTokenizeException ignored) {}

      if (value == null || isUpcomingCharInvalidNumberTermination(input))
        throw new SelectorParseException(input, valueBeginPosition, SelectorParseError.MALFORMED_NUMBER);
    }

    else
      return null;

    return new NumericValue(rawValue, value, value instanceof Double, isNegative, isNegated);
  }

  private static ArgumentValue validateRange(NumericRangeValue range) {
    if (range.startInclusive.isNegated)
        throw new SelectorParseException(range.startInclusive.raw, range.startInclusive.raw.startInclusive, SelectorParseError.RANGE_START_NEGATED);

    if (range.endInclusive.isNegated)
      throw new SelectorParseException(range.endInclusive.raw, range.endInclusive.raw.startInclusive, SelectorParseError.RANGE_END_NEGATED);

    if (range.startInclusive.value instanceof Double || range.endInclusive.value instanceof Double) {
      if (range.endInclusive.value.doubleValue() - range.startInclusive.value.doubleValue() < .001)
        throw new SelectorParseException(range.startInclusive.raw, range.startInclusive.raw.startInclusive, SelectorParseError.RANGE_START_GREATER_THAN_END);

      return range;
    }

    if (range.startInclusive.value.longValue() > range.endInclusive.value.longValue())
      throw new SelectorParseException(range.startInclusive.raw, range.startInclusive.raw.startInclusive, SelectorParseError.RANGE_START_GREATER_THAN_END);

    return range;
  }
}
