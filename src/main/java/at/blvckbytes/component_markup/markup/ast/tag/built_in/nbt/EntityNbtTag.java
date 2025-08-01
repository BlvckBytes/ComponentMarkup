/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

public class EntityNbtTag extends NbtTag {

  public EntityNbtTag() {
    super(NbtSource.ENTITY, "entity-nbt", "selector");
  }
}
