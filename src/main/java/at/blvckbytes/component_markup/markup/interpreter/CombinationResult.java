/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.constructor.ComponentConstructor;
import org.jetbrains.annotations.Nullable;

public class CombinationResult<B> {

  public static final CombinationResult<?> NO_OP_SENTINEL = new CombinationResult<>(null, null);

  public final B component;
  public final @Nullable ComputedStyle styleToApply;

  public CombinationResult(B component, @Nullable ComputedStyle styleToApply) {
    this.component = component;
    this.styleToApply = styleToApply;
  }

  public static <B, C> CombinationResult<B> empty(ComponentConstructor<B, C> componentConstructor) {
    return new CombinationResult<>(componentConstructor.createTextComponent(""), null);
  }
}
