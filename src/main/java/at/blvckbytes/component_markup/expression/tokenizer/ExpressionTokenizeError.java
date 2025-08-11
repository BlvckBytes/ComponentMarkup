/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.ErrorMessage;

public enum ExpressionTokenizeError implements ErrorMessage {
  UNTERMINATED_TEMPLATE_LITERAL_INTERPOLATION("This template-literal placeholder misses its closing-bracket: }"),
  UNESCAPED_TEMPLATE_LITERAL_CURLY("Plain curly-brackets within template-literals need to be escaped: \\{ or \\}"),
  EMPTY_TEMPLATE_LITERAL_INTERPOLATION("This template-literal placeholder must contain an expression"),
  UNTERMINATED_STRING("This string misses its closing-quote: '"),
  MALFORMED_IDENTIFIER("The name is malformed: cannot start with digits or underscores, may only contain a-z, 0-9 and underscores"),
  EXPECTED_DECIMAL_DIGITS("Expected there to be digits after the decimal-dot (.)"),
  ;

  private final String message;

  ExpressionTokenizeError(String message) {
    this.message = message;
  }

  @Override
  public String getErrorMessage() {
    return this.message;
  }
}
