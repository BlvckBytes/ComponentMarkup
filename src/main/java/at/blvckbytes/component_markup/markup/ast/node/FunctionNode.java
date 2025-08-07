/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.StringView;

import java.util.function.Function;

public class FunctionNode extends MarkupNode {

  public final Function<Interpreter, Object> function;

  public FunctionNode(StringView positionProvider, Function<Interpreter, Object> function) {
    super(positionProvider, null, null);

    this.function = function;
  }
}
