/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface DirectFieldAccess {

  Object UNKNOWN_FIELD_SENTINEL = new Object();

  @Nullable Object accessField(String rawIdentifier);

  @Nullable Set<String> getAvailableFields();

}
