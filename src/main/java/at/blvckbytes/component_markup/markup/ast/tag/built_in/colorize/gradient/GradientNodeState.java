/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionList;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeFlag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNodeState;
import at.blvckbytes.component_markup.platform.AnsiStyleColor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.platform.PackedColor;
import at.blvckbytes.component_markup.util.StringView;

import java.util.EnumSet;
import java.util.List;

public class GradientNodeState extends ColorizeNodeState {

  private final GradientGenerator gradientGenerator;

  public GradientNodeState(
    StringView tagName,
    ExpressionList colors,
    ExpressionList offsets,
    ExpressionList zIndices,
    double phase,
    EnumSet<ColorizeFlag> flags,
    Interpreter interpreter
  ) {
    super(tagName, phase, flags);

    this.gradientGenerator = new GradientGenerator(
      evaluatePackedColors(colors, interpreter),
      evaluateOffsets(offsets, interpreter),
      evaluateZIndices(zIndices, interpreter)
    );
  }

  private long[] evaluateZIndices(ExpressionList zIndices, Interpreter interpreter) {
    List<ExpressionNode> offsetList = zIndices.get(interpreter);

    long[] result = new long[offsetList.size()];

    for (int index = 0; index < result.length; ++index)
      result[index] = interpreter.evaluateAsLong(offsetList.get(index));

    return result;
  }

  private double[] evaluateOffsets(ExpressionList offsets, Interpreter interpreter) {
    List<ExpressionNode> offsetList = offsets.get(interpreter);

    double[] result = new double[offsetList.size()];

    for (int index = 0; index < result.length; ++index)
      result[index] = interpreter.evaluateAsDouble(offsetList.get(index));

    return result;
  }

  private long[] evaluatePackedColors(ExpressionList colors, Interpreter interpreter) {
    List<ExpressionNode> colorList = colors.get(interpreter);
    long[] result = new long[colorList.size()];

    for (int i = 0; i < result.length; ++i) {
      String colorString = interpreter.evaluateAsStringOrNull(colorList.get(i));

      if (colorString == null)
        continue;

      long componentColor = PackedColor.tryParse(colorString);

      if (componentColor == PackedColor.NULL_SENTINEL)
        componentColor = AnsiStyleColor.WHITE.packedColor;

      result[i] = componentColor;
    }

    return result;
  }

  @Override
  protected long getPackedColor(double progressionPercentage) {
    return gradientGenerator.getPackedColor(progressionPercentage);
  }
}
