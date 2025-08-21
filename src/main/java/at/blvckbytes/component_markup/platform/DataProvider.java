/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

import at.blvckbytes.component_markup.platform.coordinates.Coordinates;
import at.blvckbytes.component_markup.platform.selector.TargetSelector;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DataProvider {

  List<PlatformEntity> executeSelector(TargetSelector selector, Coordinates origin, @Nullable PlatformEntity self);

  @Nullable Object resolveScore(String name, String objective, @Nullable String type);

}
