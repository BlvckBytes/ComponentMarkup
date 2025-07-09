package at.blvckbytes.component_markup.markup.ast.node.style;

import at.blvckbytes.component_markup.expression.ast.BranchingNode;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.InfixOperationNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.token.NullToken;
import org.jetbrains.annotations.Nullable;

public class NodeStyle {

  private static final TerminalNode NULL_TERMINAL = new TerminalNode(new NullToken(0, "null"));

  private @Nullable ExpressionNode @Nullable [] formatStates;
  public @Nullable ExpressionNode color;
  public @Nullable ExpressionNode shadowColor;
  public @Nullable ExpressionNode shadowColorOpacity;
  public @Nullable ExpressionNode font;
  public @Nullable ExpressionNode reset;

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

    if (this.shadowColorOpacity == null && (otherValue = other.shadowColorOpacity) != null) {
      if (condition != null)
        otherValue = new BranchingNode(condition, otherValue, NULL_TERMINAL);

      this.shadowColorOpacity = otherValue;
    }

    if (this.font == null && (otherValue = other.font) != null) {
      if (condition != null)
        otherValue = new BranchingNode(condition, otherValue, NULL_TERMINAL);

      this.font = otherValue;
    }

    if (other.reset != null)
      this.reset = inheritFlagWithCondition(this.reset, other.reset, condition);

    if (other.formatStates != null) {
      for (Format format : Format.VALUES) {
        otherValue = other.getFormat(format);

        ExpressionNode thisFormatState = getFormat(format);

        setFormat(format, inheritFlagWithCondition(thisFormatState, otherValue, condition));
      }
    }
  }

  private static @Nullable ExpressionNode inheritFlagWithCondition(@Nullable ExpressionNode thisValue, @Nullable ExpressionNode otherValue, @Nullable ExpressionNode otherCondition) {
    if (otherValue == null)
      return thisValue;

    if (otherCondition != null)
      otherValue = new BranchingNode(otherCondition, otherValue, NULL_TERMINAL);

    if (thisValue == null)
      return otherValue;

    return new InfixOperationNode(thisValue, InfixOperator.DISJUNCTION, otherValue, null);
  }

  public boolean hasNonNullProperties() {
    if (this.font != null || this.color != null || this.shadowColor != null || this.shadowColorOpacity != null || this.reset != null)
      return true;

    if (this.formatStates != null) {
      for (ExpressionNode formatState : formatStates) {
        if (formatState != null)
          return true;
      }
    }

    return false;
  }

  public void setFormat(Format format, ExpressionNode value) {
    if (this.formatStates == null)
      this.formatStates = new ExpressionNode[Format.COUNT];

    this.formatStates[format.ordinal()] = value;
  }

  public @Nullable ExpressionNode getFormat(Format format) {
    if (this.formatStates == null)
      return null;

    return this.formatStates[format.ordinal()];
  }
}
