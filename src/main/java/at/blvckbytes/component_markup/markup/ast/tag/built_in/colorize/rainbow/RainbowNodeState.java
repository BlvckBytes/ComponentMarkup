/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.rainbow;

import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeFlag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNodeState;
import at.blvckbytes.component_markup.util.StringView;

import java.util.EnumSet;

public class RainbowNodeState extends ColorizeNodeState {

  private static final RainbowGenerator rainbowGenerator = new RainbowGenerator();

  public RainbowNodeState(StringView tagName, int initialSubtreeDepth, double phase, EnumSet<ColorizeFlag> flags) {
    super(tagName, initialSubtreeDepth, phase, flags);
  }

  @Override
  protected long getPackedColor(double progressionPercentage) {
    return rainbowGenerator.getPackedColor(progressionPercentage);
  }
}
