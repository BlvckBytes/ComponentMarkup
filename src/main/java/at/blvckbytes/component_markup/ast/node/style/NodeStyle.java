package at.blvckbytes.component_markup.ast.node.style;

import at.blvckbytes.component_markup.ast.ImmediateExpression;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.Jsonifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class NodeStyle extends Jsonifiable {

  public final ExpressionNode[] formatStates;
  public @Nullable ExpressionNode color;
  public @Nullable ExpressionNode font;

  public NodeStyle() {
    this.formatStates = new ExpressionNode[Format.VALUES.size()];
    Arrays.fill(formatStates, ImmediateExpression.ofNull());
  }

  public void inheritFrom(NodeStyle other) {
    if (this.color == null)
      this.color = other.color;

    if (this.font == null)
      this.font = other.font;

    for (Format format : Format.VALUES) {
      ExpressionNode thisFormatState = this.formatStates[format.ordinal()];

      if (thisFormatState != ImmediateExpression.ofNull())
        continue;

      this.formatStates[format.ordinal()] = other.formatStates[format.ordinal()];
    }
  }

  public boolean hasEffect() {
    if (this.font != null || this.color != null)
      return true;

    for (ExpressionNode formatState : formatStates) {
      if (formatState != ImmediateExpression.ofNull())
        return true;
    }

    return false;
  }

  public void setFormat(Format formatting, ExpressionNode value) {
    formatStates[formatting.ordinal()] = value;
  }

  public void reset() {
    Arrays.fill(formatStates, ImmediateExpression.ofNull());
    this.color = null;
    this.font = null;
  }
}
