/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.platform.selector.argument.ArgumentName;
import at.blvckbytes.component_markup.util.MessagePlaceholders;

import java.util.function.Function;

public enum SelectorParseError {
  MISSING_AT_SYMBOL(args -> "A selector needs to begin with an at-symbol: @"),
  MISSING_TARGET_TYPE(args -> "The at-symbol needs to be followed up by one of: " + TargetType.NAMES_STRING),
  UNKNOWN_TARGET_TYPE(args -> "Unknown target-type \"" + args.get(0) + "\"; choose one of: " + TargetType.NAMES_STRING),
  MISSING_ARGUMENTS_OPENING_BRACKET(args -> "Expected there to be an opening-bracket after the target-selector: ["),
  MISSING_ARGUMENTS_CLOSING_BRACKET(args -> "The attribute-list needs to be ending in a closing-bracket: ]"),
  MISSING_ARGUMENT_NAME(args -> "Encountered an equals-sign = without a prior argument-name"),
  DANGLING_ARGUMENT_SEPARATOR(args -> "Encountered an argument-separator comma , without a prior key/value-pair"),
  UNKNOWN_ARGUMENT_NAME(args -> "Unknown argument-name \"" + args.get(0) + "\"; choose one of: " + ArgumentName.NAMES_STRING),
  MISSING_EQUALS_SIGN(args -> "Expected there to be an equals-sign = after the argument-name \"" + args.get(0) + "\""),
  MISSING_ARGUMENT_SEPARATOR(args -> "Expected there to be an argument-separator comma , or a closing-bracket ] after the value of the argument \"" + args.get(0) + "\""),
  MULTI_NEVER_ARGUMENT(args -> "The argument \"" + args.get(0) + "\" can only occur once"),
  MULTI_IF_NEGATED_ARGUMENT(args -> "The argument \"" + args.get(0) + "\" can only occur multiple times if negated"),
  STRING_CONTAINED_QUOTE(args -> "Strings cannot contain quotes without a prior backslash: \\\""),
  UNTERMINATED_STRING(args -> "Strings beginning with double-quotes \" need to again end in such"),
  VALIDATION_FAILED_IS_NEGATIVE(args -> "The argument \"" + args.get(0) + "\" does not support negative values"),
  VALIDATION_FAILED_IS_FRACTIONAL(args -> "The argument \"" + args.get(0) + "\" does not support fractional values"),
  VALIDATION_FAILED_IS_NEGATED(args -> "The argument \"" + args.get(0) + "\" does not support negated values"),
  VALIDATION_FAILED_IS_RANGE(args -> "The argument \"" + args.get(0) + "\" does not support ranges"),
  VALIDATION_FAILED_IS_RANGE_START_NEGATIVE(args -> "The argument \"" + args.get(0) + "\" does not support ranges with negative values (start)"),
  VALIDATION_FAILED_IS_RANGE_END_NEGATIVE(args -> "The argument \"" + args.get(0) + "\" does not support ranges with negative values (end)"),
  VALIDATION_FAILED_IS_RANGE_START_FRACTIONAL(args -> "The argument \"" + args.get(0) + "\" does not support ranges with fractional values (start)"),
  VALIDATION_FAILED_IS_RANGE_END_FRACTIONAL(args -> "The argument \"" + args.get(0) + "\" does not support ranges with fractional values (end)"),
  VALIDATION_FAILED_IS_NON_NUMERIC(args -> "The argument \"" + args.get(0) + "\" requires a numeric-value"),
  VALIDATION_FAILED_IS_NON_NUMERIC_OR_RANGE(args -> "The argument \"" + args.get(0) + "\" requires a numeric- or range-value"),
  VALIDATION_FAILED_IS_NON_SORT_CRITERION(args -> "The argument \"" + args.get(0) + "\" must have a value of one of: " + SortCriterion.NAMES_STRING),
  VALIDATION_FAILED_IS_BLANK_STRING(args -> "The argument \"" + args.get(0) + "\" does not accept empty or blank strings"),
  DANGLING_MINUS_SIGN(args -> "Encountered a dangling minus-sign - in front of the range-operator"),
  DANGLING_BANG(args -> "Encountered a dangling exclamation-mark ! in front of the range-operator"),
  EXPECTED_RHS_OF_RANGE(args -> "A range-operator without a left-hand-side must have a right-hand-side value"),
  EXPECTED_RANGE_OPERATOR(args -> "Two subsequent numeric values need to be joined by a range-operator: .."),
  DOUBLE_RANGE_OPERATOR(args -> "The range-operator may only be used once per value"),
  MALFORMED_NUMBER(args -> "This number is malformed"),
  RANGE_START_NEGATED(args -> "Ranges do not support negated values; this start-value is negated"),
  RANGE_END_NEGATED(args -> "Ranges do not support negated values; this end-value is negated"),
  RANGE_START_GREATER_THAN_END(args -> "The start-value of a range cannot be greater than it's end-value"),
  ;

  public final Function<MessagePlaceholders, String> messageBuilder;

  SelectorParseError(Function<MessagePlaceholders, String> messageBuilder) {
    this.messageBuilder = messageBuilder;
  }
}
