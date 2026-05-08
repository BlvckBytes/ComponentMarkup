/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.function.Function;

public class FunctionDrivenNode extends MarkupNode {

  public final Function<Interpreter<?, ?>, Object> function;

  public FunctionDrivenNode(
    InputView positionProvider,
    @Nullable LinkedHashSet<LetBinding> letBindings,
    Function<Interpreter<?, ?>, Object> function
  ) {
    super(positionProvider, null, letBindings);

    this.function = function;
  }
}
