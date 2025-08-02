/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.platform.selector.argument.ArgumentValue;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SortCriterion implements ArgumentValue {
  NEAREST("nearest"),
  FURTHEST("farthest"),
  RANDOM("random"),
  ARBITRARY("arbitrary"),
  ;

  public static final String NAMES_STRING = Arrays.stream(values())
    .map(it -> it.stringValue)
    .collect(Collectors.joining(", "));

  public final String stringValue;

  SortCriterion(String stringValue) {
    this.stringValue = stringValue;
  }
}
