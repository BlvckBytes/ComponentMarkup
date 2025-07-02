package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.gradient;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionList;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeFlag;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize.ColorizeNodeState;
import at.blvckbytes.component_markup.markup.interpreter.AnsiStyleColor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.PackedColor;

import java.util.EnumSet;
import java.util.List;

public class GradientNodeState extends ColorizeNodeState {

  private final GradientGenerator gradientGenerator;

  public GradientNodeState(
    String tagNameLower,
    ExpressionList colors,
    ExpressionList offsets,
    ExpressionList zIndices,
    double phase,
    EnumSet<ColorizeFlag> flags,
    Interpreter interpreter
  ) {
    super(tagNameLower, phase, flags);

    this.gradientGenerator = new GradientGenerator(
      evaluateColors(colors, interpreter),
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

  private int[] evaluateColors(ExpressionList colors, Interpreter interpreter) {
    List<ExpressionNode> colorList = colors.get(interpreter);
    int[] result = new int[colorList.size()];

    for (int i = 0; i < result.length; ++i) {
      String colorString = interpreter.evaluateAsStringOrNull(colorList.get(i));

      if (colorString == null)
        continue;

      int componentColor = PackedColor.tryParse(colorString);

      if (componentColor == PackedColor.NULL_SENTINEL)
        componentColor = AnsiStyleColor.WHITE.packedColor;

      result[i] = componentColor;
    }

    return result;
  }

  @Override
  protected int getColor(double progressionPercentage) {
    return gradientGenerator.getColor(progressionPercentage);
  }
}
