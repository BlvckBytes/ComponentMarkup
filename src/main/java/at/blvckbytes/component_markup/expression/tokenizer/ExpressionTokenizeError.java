/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.util.MessagePlaceholders;

import java.util.function.Function;

public enum ExpressionTokenizeError {
  UNTERMINATED_TEMPLATE_LITERAL_INTERPOLATION(args -> "This template-literal placeholder misses its closing-bracket: }"),
  UNESCAPED_TEMPLATE_LITERAL_CURLY(args -> "Plain curly-brackets within template-literals need to be escaped: \\{ or \\}"),
  EMPTY_TEMPLATE_LITERAL_INTERPOLATION(args -> "This template-literal placeholder must contain an expression"),
  UNTERMINATED_STRING(args -> "This string misses its closing-quote: " + args.get(0)),
  MALFORMED_IDENTIFIER(args -> "The name is malformed: cannot start with digits or underscores, may only contain a-z, 0-9 and underscores"),
  EXPECTED_DECIMAL_DIGITS(args -> "Expected there to be digits after the decimal-dot (.)"),
  ;

  public final Function<MessagePlaceholders, String> messageBuilder;

  ExpressionTokenizeError(Function<MessagePlaceholders, String> messageBuilder) {
    this.messageBuilder = messageBuilder;
  }
}
