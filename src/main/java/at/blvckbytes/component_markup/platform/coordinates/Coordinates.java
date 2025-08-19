/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.coordinates;

import org.jetbrains.annotations.Nullable;

public class Coordinates {

  public final double x, y, z;
  public final @Nullable String world;

  public Coordinates(double x, double y, double z, @Nullable String world) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.world = world;
  }
}
