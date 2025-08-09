/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression;

import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.token.*;
import at.blvckbytes.component_markup.util.StringView;

public class ImmediateExpression {

  public static TerminalNode ofNull() {
    return new TerminalNode(new NullToken(StringView.EMPTY));
  }

  public static TerminalNode ofDouble(StringView raw, double value) {
    return new TerminalNode(new DoubleToken(raw, value));
  }

  public static TerminalNode ofBoolean(StringView raw, boolean value) {
    return new TerminalNode(new BooleanToken(raw, value));
  }

  public static TerminalNode ofLong(StringView raw, long value) {
    return new TerminalNode(new LongToken(raw, value));
  }

  public static TerminalNode ofString(StringView raw, String value) {
    return new TerminalNode(new StringToken(raw, value));
  }
}
