/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.nbt;

public class BlockNbtTag extends NbtTag {

  public BlockNbtTag() {
    super(NbtSource.BLOCK, "block-nbt", "coordinates");
  }
}
