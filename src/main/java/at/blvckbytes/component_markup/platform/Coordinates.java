/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

import at.blvckbytes.component_markup.markup.interpreter.DirectFieldAccess;
import org.jetbrains.annotations.Nullable;

public class Coordinates implements DirectFieldAccess {

  public final double x, y, z;
  public final @Nullable String world;

  public Coordinates(double x, double y, double z, @Nullable String world) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.world = world;
  }

  @Override
  public @Nullable Object accessField(String rawIdentifier) {
    switch (rawIdentifier) {
      case "x":
        return x;
      case "y":
        return y;
      case "z":
        return z;
      case "world":
        return world;
      default:
        return DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
    }
  }
}
