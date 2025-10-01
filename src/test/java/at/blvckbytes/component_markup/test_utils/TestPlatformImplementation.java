/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.test_utils;

import at.blvckbytes.component_markup.markup.interpreter.JsonComponentConstructor;
import at.blvckbytes.component_markup.platform.*;

public class TestPlatformImplementation implements PlatformImplementation {

  public static final TestPlatformImplementation INSTANCE = new TestPlatformImplementation();

  private static final ComponentConstructor componentConstructor = new JsonComponentConstructor();

  private TestPlatformImplementation() {}

  @Override
  public ComponentConstructor getComponentConstructor() {
    return componentConstructor;
  }

  public static SlotContext getSlotContext(SlotType slot) {
    return INSTANCE.getComponentConstructor().getSlotContext(slot);
  }
}
