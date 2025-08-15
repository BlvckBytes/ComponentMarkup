/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

import at.blvckbytes.component_markup.markup.interpreter.DirectFieldAccess;
import at.blvckbytes.component_markup.platform.selector.TargetSelector;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class PlatformEntity implements DirectFieldAccess {

  public final String name;
  public final String displayName;
  public final UUID uuid;

  protected PlatformEntity(String name, String displayName, UUID uuid) {
    this.name = name;
    this.displayName = displayName;
    this.uuid = uuid;
  }

  public abstract int x();

  public abstract int y();

  public abstract int z();

  public abstract List<PlatformEntity> executeSelector(TargetSelector selector);

  @Override
  public @Nullable Object accessField(String rawIdentifier) {
    switch (rawIdentifier) {
      case "x":
        return x();
      case "y":
        return y();
      case "z":
        return z();
      default:
        return DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
    }
  }
}
