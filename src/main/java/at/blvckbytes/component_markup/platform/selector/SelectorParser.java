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
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SelectorParser {

  // TODO: Extensively test parsing attribute-values (and keys etc., but that has utmost importance)
  // TODO: Replace all illegal-state exceptions with proper selector-errors

  private static final ArgumentValue RANGE_VALUE_SENTINEL = () -> false;

  public static @NotNull TargetSelector parse(StringView input) {
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

    StringView rawTarget = input.buildSubViewAbsolute(typeBegin, input.getPosition() + 1);
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

  private static List<ArgumentEntry> parseArguments(StringView input) {
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

      StringView rawName = input.buildSubViewAbsolute(nameBegin, input.getPosition() + 1);

      ArgumentName name = ArgumentName.ofName(rawName);

      if (name == null)
        throw new SelectorParseException(input, nameBegin, SelectorParseError.UNKNOWN_ARGUMENT_NAME, rawName.buildString());

      input.consumeWhitespace(null);

      if (input.peekChar(0) != '=')
        throw new SelectorParseException(input, SelectorParseError.MISSING_EQUALS_SIGN, rawName.buildString());

      input.nextChar();
      input.consumeWhitespace(null);

      ArgumentValue value = parseArgumentValue(input, name);
      ValidationFailure validationFailure = name.typeErrorProvider.apply(value);

      if (validationFailure != null)
        throw new IllegalStateException("Validation-Failure: " + validationFailure);

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

  private static ArgumentValue parseArgumentValue(StringView input, ArgumentName name) {
    char firstChar = input.peekChar(0);

    if (firstChar == '"' || firstChar == '!' && input.peekChar(1) == '"') {
      try {
        boolean isNegated = false;

        if (firstChar == '!') {
          isNegated = true;
          input.nextChar();
        }

        ExpressionTokenizer expressionTokenizer = new ExpressionTokenizer(input, null);
        return new StringValue(expressionTokenizer.parseStringToken().raw, isNegated);
      } catch (ExpressionTokenizeException e) {
        // That's the only reason as to why this tokenizer-method would throw
        throw new SelectorParseException(input, e.position, SelectorParseError.UNTERMINATED_STRING);
      }
    }

    if (name.supportsStrings) {
      boolean isNegated = false;

      if (firstChar == '!') {
        isNegated = true;
        input.nextChar();
      }

      else
        input.nextChar();

      int startIndex = input.getPosition();
      char upcomingChar;

      while ((upcomingChar = input.peekChar(0)) != 0) {
        if (Character.isWhitespace(upcomingChar))
          break;

        if (upcomingChar == ',' || upcomingChar == ']')
          break;

        char priorChar = input.priorChar(0);

        if (input.nextChar() == '"') {
          if (priorChar == '\\') {
            input.addIndexToBeRemoved(input.getPosition());
            continue;
          }

          throw new SelectorParseException(input, SelectorParseError.STRING_CONTAINED_QUOTE);
        }
      }

      return new StringValue(input.buildSubViewAbsolute(startIndex, input.getPosition() + 1), isNegated);
    }

    NumericValue firstNumber;
    ArgumentValue currentValue = parseNumberOrRangeOperator(input);

    if (currentValue == null)
      throw new IllegalStateException("Expected numeric value of argument " + name.name);

    if (currentValue instanceof NumericValue) {
      if (input.peekChar(0) != '.')
        return currentValue;

      firstNumber = (NumericValue) currentValue;
    }

    // value is range-operator
    else {
      currentValue = parseNumberOrRangeOperator(input);

      if (currentValue == null)
        throw new IllegalStateException("Expected numeric rhs of range-operator");

      if (currentValue instanceof NumericValue)
        return validateRange(new NumericRangeValue(null, (NumericValue) currentValue));

      throw new IllegalStateException("Two back-to-back range-operators");
    }

    currentValue = parseNumberOrRangeOperator(input);

    if (currentValue != RANGE_VALUE_SENTINEL)
      throw new IllegalStateException("Expected range-operator");

    currentValue = parseNumberOrRangeOperator(input);

    if (currentValue == null)
      return validateRange(new NumericRangeValue(firstNumber, null));

    if (currentValue == RANGE_VALUE_SENTINEL)
      throw new IllegalStateException("Two back-to-back range-operators");

    return validateRange(new NumericRangeValue(firstNumber, (NumericValue) currentValue));
  }

  private static @Nullable ArgumentValue parseNumberOrRangeOperator(StringView input) {
    ExpressionTokenizer expressionTokenizer = new ExpressionTokenizer(input, null);

    char upcomingChar = input.peekChar(0);
    int valueBeginPosition = -1;

    boolean isNegated = false;

    if (upcomingChar == '!') {
      isNegated = true;
      input.nextChar();
      upcomingChar = input.peekChar(0);
      valueBeginPosition = input.getPosition();
    }

    boolean isNegative = false;

    if (upcomingChar == '-') {
      isNegative = true;
      input.nextChar();
      upcomingChar = input.peekChar(0);

      if (valueBeginPosition < 0)
        valueBeginPosition = input.getPosition();
    }

    if (valueBeginPosition < 0)
      valueBeginPosition = input.getPosition() + 1;

    StringView rawValue = null;
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

      if (value == null)
        throw new IllegalStateException("Malformed number at " + valueBeginPosition);
    }

    else if (upcomingChar == '.') {
      if (input.peekChar(1) == '.') {
        input.nextChar();
        input.nextChar();

        if (isNegative)
          throw new IllegalStateException("Dangling minus-sign");

        if (isNegated)
          throw new IllegalStateException("Dangling bang");

        return RANGE_VALUE_SENTINEL;
      }

      try {
        Token result = expressionTokenizer.tryParseDotDoubleToken();

        if (result instanceof DoubleToken) {
          value = ((DoubleToken) result).value;
          rawValue = result.raw;
        }
      } catch (ExpressionTokenizeException ignored) {}

      if (value == null)
        throw new IllegalStateException("Malformed number at " + valueBeginPosition);
    }

    else
      return null;

    return new NumericValue(rawValue, value, value instanceof Double, isNegative, isNegated);
  }

  private static ArgumentValue validateRange(NumericRangeValue range) {
    if (range.startInclusive.isNegated)
        throw new IllegalStateException("Start negated");

    if (range.endInclusive.isNegated)
      throw new IllegalStateException("End negated");

    if (range.startInclusive.value instanceof Double || range.endInclusive.value instanceof Double) {
      if (Math.abs(range.endInclusive.value.doubleValue() - range.startInclusive.value.doubleValue()) < .001)
        throw new IllegalStateException("Start > end");

      return range;
    }

    if (range.startInclusive.value.longValue() > range.endInclusive.value.longValue())
      throw new IllegalStateException("Start > end");

    return range;
  }
}
