package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ColorizeAttributes {

  private final @Nullable ExpressionNode phase;
  private final @Nullable ExpressionNode deep;
  private final @Nullable ExpressionNode overrideColors;
  private final @Nullable ExpressionNode skipWhitespace;
  private final @Nullable ExpressionNode skipNonText;
  private final @Nullable ExpressionNode mergeInner;

  public ColorizeAttributes(
    @Nullable ExpressionNode phase,
    @Nullable ExpressionNode deep,
    @Nullable ExpressionNode overrideColors,
    @Nullable ExpressionNode skipWhitespace,
    @Nullable ExpressionNode skipNonText,
    @Nullable ExpressionNode mergeInner
  ) {
    this.phase = phase;
    this.deep = deep;
    this.overrideColors = overrideColors;
    this.skipWhitespace = skipWhitespace;
    this.skipNonText = skipNonText;
    this.mergeInner = mergeInner;
  }

  public double getPhase(Interpreter interpreter) {
    return this.phase == null ? 0 : interpreter.evaluateAsDouble(this.phase);
  }

  public EnumSet<ColorizeFlag> getFlags(Interpreter interpreter) {
    EnumSet<ColorizeFlag> result = EnumSet.noneOf(ColorizeFlag.class);

    if (this.deep != null && interpreter.evaluateAsTriState(this.deep) == TriState.TRUE)
      result.add(ColorizeFlag.DEEP);

    if (this.overrideColors != null && interpreter.evaluateAsTriState(this.overrideColors) == TriState.TRUE)
      result.add(ColorizeFlag.OVERRIDE_COLORS);

    if (this.skipWhitespace == null || interpreter.evaluateAsTriState(this.skipWhitespace) != TriState.FALSE)
      result.add(ColorizeFlag.SKIP_WHITESPACE);

    if (this.skipNonText != null && interpreter.evaluateAsTriState(this.skipNonText) == TriState.TRUE)
      result.add(ColorizeFlag.SKIP_NON_TEXT);

    if (this.mergeInner != null && interpreter.evaluateAsTriState(this.mergeInner) == TriState.TRUE)
      result.add(ColorizeFlag.MERGE_INNER);

    return result;
  }
}
