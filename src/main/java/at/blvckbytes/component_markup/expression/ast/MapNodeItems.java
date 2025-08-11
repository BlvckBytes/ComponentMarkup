/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.ast;

import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MapNodeItems {

  private final Map<String, ExpressionNode> valueByName = new LinkedHashMap<>();

  // Only kept for test-matching, as to compare against real views
  // This is not ideal, but I need to move on now...
  private final List<IdentifierToken> keys = new ArrayList<>();

  public @Nullable ExpressionNode put(IdentifierToken key, ExpressionNode value) {
    keys.add(key);
    return valueByName.put(key.raw.buildString(), value);
  }

  public Set<Map.Entry<String, ExpressionNode>> entrySet() {
    return valueByName.entrySet();
  }

  public boolean isEmpty() {
    return valueByName.isEmpty();
  }
}
