/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform;

public interface PlatformImplementation {

  ComponentConstructor getComponentConstructor();

  DataProvider getDataProvider();

}
