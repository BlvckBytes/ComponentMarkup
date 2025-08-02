/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.platform.selector.argument.ArgumentName;

import java.util.function.Function;

public enum SelectorParseError {
  MISSING_AT_SYMBOL(args -> "A selector needs to begin with an at-symbol: @"),
  MISSING_TARGET_TYPE(args -> "The at-symbol needs to be followed up by one of: " + TargetType.NAMES_STRING),
  UNKNOWN_TARGET_TYPE(args -> "Unknown target-type \"" + args[0] + "\"; choose one of: " + TargetType.NAMES_STRING),
  MISSING_ARGUMENTS_OPENING_BRACKET(args -> "Expected there to be an opening-bracket after the target-selector: ["),
  MISSING_ARGUMENTS_CLOSING_BRACKET(args -> "The attribute-list needs to be ending in a closing-bracket: ]"),
  MISSING_ARGUMENT_NAME(args -> "Encountered an equals-sign = without a prior argument-name"),
  DANGLING_ARGUMENT_SEPARATOR(args -> "Encountered an argument-separator comma , without a prior key/value-pair"),
  UNKNOWN_ARGUMENT_NAME(args -> "Unknown argument-name \"" + args[0] + "\"; choose one of: " + ArgumentName.NAMES_STRING),
  MISSING_EQUALS_SIGN(args -> "Expected there to be an equals-sign = after the argument-name \"" + args[0] + "\""),
  MISSING_ARGUMENT_SEPARATOR(args -> "Expected there to be an argument-separator comma , or a closing-bracket ] after the value of the argument \"" + args[0] + "\""),
  MULTI_NEVER_ARGUMENT(args -> "The argument \"" + args[0] + "\" can only occur once"),
  MULTI_IF_NEGATED_ARGUMENT(args -> "The argument \"" + args[0] + "\" can only occur multiple times if negated"),
  STRING_CONTAINED_QUOTE(args -> "Strings cannot contain quotes without a prior backslash: \\\""),
  UNTERMINATED_STRING(args -> "Strings beginning with double-quotes \" need to again end in such"),
  ;

  public final Function<String[], String> messageBuilder;

  SelectorParseError(Function<String[], String> messageBuilder) {
    this.messageBuilder = messageBuilder;
  }
}
