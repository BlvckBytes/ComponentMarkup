/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.style;

import at.blvckbytes.component_markup.platform.PlatformFeature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Format {
  OBFUSCATED(PlatformFeature.OBFUSCATED_FORMAT),
  BOLD(PlatformFeature.BOLD_FORMAT),
  STRIKETHROUGH(PlatformFeature.STRIKETHROUGH_FORMAT),
  UNDERLINED(PlatformFeature.UNDERLINED_FORMAT),
  ITALIC(PlatformFeature.ITALIC_FORMAT);

  public static final List<Format> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  public static final int COUNT = VALUES.size();

  public final PlatformFeature feature;

  Format(PlatformFeature feature) {
    this.feature = feature;
  }
}
