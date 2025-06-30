package at.blvckbytes.component_markup.markup.ast.tag.built_in.gradient;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.interpreter.AnsiStyleColor;
import at.blvckbytes.component_markup.markup.interpreter.ComponentColor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GradientNodeState {

  public final Stack<List<Object>> injectedComponentsStack;
  public final boolean deep;
  public final GradientGenerator gradientGenerator;

  public GradientNodeState(
    List<ExpressionNode> colors,
    List<ExpressionNode> offsets,
    @Nullable ExpressionNode deep,
    Interpreter interpreter
  ) {
    this.injectedComponentsStack = new Stack<>();
    this.deep = deep != null && interpreter.evaluateAsBoolean(deep);

    this.gradientGenerator = new GradientGenerator(
      evaluateColors(colors, interpreter),
      evaluateOffsets(offsets, interpreter)
    );
  }

  private double[] evaluateOffsets(List<ExpressionNode> offsets, Interpreter interpreter) {
    double[] result = new double[offsets.size()];

    for (int index = 0; index < result.length; ++index)
      result[index] = interpreter.evaluateAsDouble(offsets.get(index));

    return result;
  }

  private List<Color> evaluateColors(List<ExpressionNode> colors, Interpreter interpreter) {
    List<Color> result = new ArrayList<>();

    for (ExpressionNode item : colors) {
      String colorString = interpreter.evaluateAsStringOrNull(item);

      if (colorString == null)
        continue;

      ComponentColor componentColor = ComponentColor.tryParse(colorString);

      if (componentColor == null)
        componentColor = AnsiStyleColor.BLACK;

      result.add(componentColor.getColor());
    }

    return result;
  }
}
