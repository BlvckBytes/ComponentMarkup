/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

import at.blvckbytes.component_markup.expression.interpreter.FieldGetter;
import at.blvckbytes.component_markup.platform.selector.TargetSelector;

import java.util.List;
import java.util.UUID;

public abstract class PlatformEntity {

  public final String name;
  public final String displayName;
  public final UUID uuid;

  protected PlatformEntity(String name, String displayName, UUID uuid) {
    this.name = name;
    this.displayName = displayName;
    this.uuid = uuid;
  }

  @FieldGetter
  public abstract int x();

  @FieldGetter
  public abstract int y();

  @FieldGetter
  public abstract int z();

  public abstract List<PlatformEntity> executeSelector(TargetSelector selector);
}
