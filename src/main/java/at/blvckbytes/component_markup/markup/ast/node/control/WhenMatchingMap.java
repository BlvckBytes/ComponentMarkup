/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhenMatchingMap {

  private final Map<String, MarkupNode> caseByValue = new HashMap<>();

  // Only kept for test-matching, as to compare against real views
  // This is not ideal, but I need to move on now...
  private final List<InputView> views = new ArrayList<>();

  public @Nullable MarkupNode put(InputView value, MarkupNode node) {
    views.add(value);
    return caseByValue.put(value.buildString().toLowerCase(), node);
  }

  public @Nullable MarkupNode get(String value) {
    return caseByValue.get(value.toLowerCase());
  }

  public boolean isEmpty() {
    return caseByValue.isEmpty();
  }
}
