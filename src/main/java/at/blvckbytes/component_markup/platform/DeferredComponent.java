/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DeferredComponent {

  @Nullable List<Object> renderDeferredComponent(@Nullable PlatformEntity recipient);

}
