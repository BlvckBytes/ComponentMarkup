/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

import org.jetbrains.annotations.NotNull;

public interface DeferredDataProvider {

  String getName(@NotNull Object recipient);

  String getDisplayName(@NotNull Object recipient);

}
