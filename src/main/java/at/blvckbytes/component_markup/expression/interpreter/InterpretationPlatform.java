/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public interface InterpretationPlatform {

  /**
   * @return {@code null} on malformed pattern, splitting-result otherwise
   */
  String[] split(String input, String delimiter, boolean regex);

  /**
   * @return {@link TriState#NULL} on malformed pattern, binary result otherwise
   */
  TriState matchesPattern(String input, String pattern);

  String asciify(String input);

  String slugify(String input);

  String toTitleCase(String input);

  String formatDate(
    String format,
    @Nullable String locale,
    @Nullable String timeZone,
    long timestamp,
    EnumSet<FormatDateWarning> warningsOutput
  );

  String formatNumber(
    String format,
    @Nullable String roundingMode,
    @Nullable String locale,
    Number number,
    EnumSet<FormatNumberWarning> warningsOutput
  );
}
