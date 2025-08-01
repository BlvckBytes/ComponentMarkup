/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util;

import org.jetbrains.annotations.Nullable;

public enum TriState {
  TRUE(true),
  FALSE(false),
  NULL(null)
  ;

  public final @Nullable Boolean bool;

  TriState(@Nullable Boolean bool) {
    this.bool = bool;
  }
}
