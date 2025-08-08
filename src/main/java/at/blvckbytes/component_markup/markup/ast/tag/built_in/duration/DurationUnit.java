/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.duration;

import org.jetbrains.annotations.Nullable;

public enum DurationUnit {
  MILLIS ('i',    1L),
  SECONDS('s', 1000L),
  MINUTES('m',   60L * 1000),
  HOURS  ('h',   60L *   60 * 1000),
  DAYS   ('d',   24L *   60 *   60 * 1000),
  WEEKS  ('w',    7L *   24 *   60 *   60 * 1000),
  MONTHS ('M',   30L *   24 *   60 *   60 * 1000),
  YEARS  ('y',  365L *   24 *   60 *   60 * 1000),
  ;

  public final char character;
  public final long milliseconds;

  DurationUnit(char character, long milliseconds) {
    this.milliseconds = milliseconds;
    this.character = character;
  }

  public static @Nullable DurationUnit fromChar(char c) {
    switch (c) {
      case 'i':
        return MILLIS;
      case 's':
        return SECONDS;
      case 'm':
        return MINUTES;
      case 'h':
        return HOURS;
      case 'd':
        return DAYS;
      case 'M':
        return MONTHS;
      case 'y':
        return YEARS;
      default:
        return null;
    }
  }
}
