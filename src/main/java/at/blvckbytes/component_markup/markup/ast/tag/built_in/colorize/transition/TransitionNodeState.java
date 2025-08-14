/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.transition;

import at.blvckbytes.component_markup.markup.ast.tag.ExpressionList;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeFlag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient.GradientNodeState;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.StringView;

import java.util.EnumSet;

public class TransitionNodeState extends GradientNodeState {

  public TransitionNodeState(
    StringView tagName,
    int initialSubtreeDepth,
    ExpressionList colors,
    ExpressionList offsets,
    ExpressionList zIndices,
    double phase,
    EnumSet<ColorizeFlag> flags,
    Interpreter interpreter
  ) {
    super(tagName, initialSubtreeDepth, colors, offsets, zIndices, phase, flags, interpreter);
  }

  @Override
  protected long getPackedColor(int index, int length) {
    return super.getPackedColor(0, 1);
  }
}
