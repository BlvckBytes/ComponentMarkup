/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum TargetType {
  NEAREST_PLAYER('p'),
  RANDOM_PLAYER('r'),
  ALL_PLAYERS('a'),
  ALL_ENTITIES('e'),
  COMMAND_EXECUTOR('s'),
  NEAREST_ENTITY('n'),
  ;

  public static final List<TargetType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

  public static final String NAMES_STRING = VALUES.stream()
    .map(x -> String.valueOf(x.character))
    .collect(Collectors.joining(", "));

  public final char character;

  TargetType(char character) {
    this.character = character;
  }

  public static @Nullable TargetType ofName(StringView name) {
    if (name.length() != 1)
      return null;

    switch (name.nthChar(0)) {
      case 'p':
        return NEAREST_PLAYER;

      case 'r':
        return RANDOM_PLAYER;

      case 'a':
        return ALL_PLAYERS;

      case 'e':
        return ALL_ENTITIES;

      case 's':
        return COMMAND_EXECUTOR;

      case 'n':
        return NEAREST_ENTITY;

      default:
        return null;
    }
  }
}
