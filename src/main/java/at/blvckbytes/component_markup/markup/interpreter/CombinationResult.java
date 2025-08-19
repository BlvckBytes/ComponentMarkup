/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.platform.ComponentConstructor;
import org.jetbrains.annotations.Nullable;

public class CombinationResult {

  public static final CombinationResult NO_OP_SENTINEL = new CombinationResult(null, null);

  public final Object component;
  public final @Nullable ComputedStyle styleToApply;

  public CombinationResult(Object component, @Nullable ComputedStyle styleToApply) {
    this.component = component;
    this.styleToApply = styleToApply;
  }

  public static CombinationResult empty(ComponentConstructor componentConstructor) {
    return new CombinationResult(componentConstructor.createTextComponent(""), null);
  }
}
