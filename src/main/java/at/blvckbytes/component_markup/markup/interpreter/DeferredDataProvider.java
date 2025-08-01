/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.NotNull;

public interface DeferredDataProvider {

  String getName(@NotNull Object recipient);

  String getDisplayName(@NotNull Object recipient);

}
