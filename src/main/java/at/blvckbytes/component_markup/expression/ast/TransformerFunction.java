/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TransformerFunction {

  /**
   * @param input The resulting value of interpreting the wrapped node
   * @param environment Current environment used to interpret
   * @return Result of transforming the input-value
   */
  @Nullable Object transform(@Nullable Object input, InterpretationEnvironment environment);

}
