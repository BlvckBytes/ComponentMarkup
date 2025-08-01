/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.interpreter.ComponentConstructor;
import at.blvckbytes.component_markup.markup.interpreter.SlotContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DeferredRenderer<Parameter extends RendererParameter> {

  /**
   * @return A list of components, where (if supported by the slot) each represents
   *         another line; return null to become invisible (no content).
   */
  @Nullable List<Object> renderComponent(
    Parameter parameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable Object recipient
  );
}
