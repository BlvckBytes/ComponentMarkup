/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MapNodeItems {

  private final Map<String, ExpressionNode> valueByName = new LinkedHashMap<>();

  public @Nullable ExpressionNode put(String key, ExpressionNode value) {
    return valueByName.put(key, value);
  }

  public Set<Map.Entry<String, ExpressionNode>> entrySet() {
    return valueByName.entrySet();
  }

  public boolean isEmpty() {
    return valueByName.isEmpty();
  }
}
