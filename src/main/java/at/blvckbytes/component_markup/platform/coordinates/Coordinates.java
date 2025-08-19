/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.coordinates;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class Coordinates {

  public final double x, y, z;
  public final @Nullable InputView world;

  public Coordinates(double x, double y, double z, @Nullable InputView world) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.world = world;
  }
}
