package at.blvckbytes.component_markup.markup.ast.node.style;

import at.blvckbytes.component_markup.expression.ast.BranchingNode;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.token.NullToken;
import at.blvckbytes.component_markup.util.Jsonifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeStyle extends Jsonifiable {

  private static final TerminalNode NULL_TERMINAL = new TerminalNode(new NullToken(0, "null"));

  public final @Nullable ExpressionNode[] formatStates;
  public @Nullable ExpressionNode color;
  public @Nullable ExpressionNode shadowColor;
  public @Nullable ExpressionNode font;

  public NodeStyle() {
    this.formatStates = new ExpressionNode[Format.VALUES.size()];
    this.reset();
  }

  public void inheritFrom(NodeStyle other, @Nullable ExpressionNode condition) {
    ExpressionNode otherValue;

    if (this.color == null && (otherValue = other.color) != null) {
      if (condition != null)
        otherValue = new BranchingNode(condition, otherValue, NULL_TERMINAL);

      this.color = otherValue;
    }

    if (this.shadowColor == null && (otherValue = other.shadowColor) != null) {
      if (condition != null)
        otherValue = new BranchingNode(condition, otherValue, NULL_TERMINAL);

      this.shadowColor = otherValue;
    }

    if (this.font == null && (otherValue = other.font) != null) {
      if (condition != null)
        otherValue = new BranchingNode(condition, otherValue, NULL_TERMINAL);

      this.font = otherValue;
    }

    for (Format format : Format.VALUES) {
      ExpressionNode thisFormatState = this.formatStates[format.ordinal()];

      if (thisFormatState != null)
        continue;

      otherValue = other.formatStates[format.ordinal()];

      if (otherValue == null)
        continue;

      if (condition != null)
        otherValue = new BranchingNode(condition, otherValue, null);

      this.formatStates[format.ordinal()] = otherValue;
    }
  }

  public boolean hasEffect() {
    if (this.font != null || this.color != null || this.shadowColor != null)
      return true;

    for (ExpressionNode formatState : formatStates) {
      if (formatState != null)
        return true;
    }

    return false;
  }

  public void setFormat(Format formatting, ExpressionNode value) {
    formatStates[formatting.ordinal()] = value;
  }

  public void reset() {
    Arrays.fill(formatStates, null);
    this.color = null;
    this.shadowColor = null;
    this.font = null;
  }
}
