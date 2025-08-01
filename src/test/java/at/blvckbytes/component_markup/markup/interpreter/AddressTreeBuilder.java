/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import java.util.function.Function;

public class AddressTreeBuilder {

  public final AddressTree result;

  public AddressTreeBuilder() {
    this.result = new AddressTree();
  }

  public AddressTreeBuilder(AddressTree result) {
    this.result = result;
  }

  public AddressTreeBuilder terminal(int index) {
    result.put(index, null);
    return this;
  }

  public AddressTreeBuilder put(int index, Function<SlotMapBuilder, SlotMapBuilder> handler) {
    result.put(index, handler.apply(new SlotMapBuilder()).result);
    return this;
  }
}
