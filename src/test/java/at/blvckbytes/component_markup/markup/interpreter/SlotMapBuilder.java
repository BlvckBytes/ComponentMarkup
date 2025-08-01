/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import java.util.EnumMap;
import java.util.function.Function;

public class SlotMapBuilder {

  public final EnumMap<MembersSlot, AddressTree> result = new EnumMap<>(MembersSlot.class);

  public SlotMapBuilder slot(MembersSlot slot, Function<AddressTreeBuilder, AddressTreeBuilder> handler) {
    handler.apply(new AddressTreeBuilder(this.result.computeIfAbsent(slot, key -> new AddressTree())));
    return this;
  }
}
