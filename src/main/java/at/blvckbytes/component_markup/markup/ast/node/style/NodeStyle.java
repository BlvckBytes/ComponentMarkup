/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.style;

import at.blvckbytes.component_markup.expression.ast.BranchingNode;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.InfixOperationNode;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class NodeStyle {

  private @Nullable ExpressionNode @Nullable [] formatStates;

  public @Nullable ExpressionNode color;
  public @Nullable ExpressionNode shadowColor;
  public @Nullable ExpressionNode shadowColorOpacity;
  public @Nullable ExpressionNode font;
  public @Nullable ExpressionNode reset;

  public void inheritFrom(NodeStyle other) {
    if (other.color != null)
      this.color = makeFallbackExpression(this.color, other.color);

    if (other.shadowColor != null)
      this.shadowColor = makeFallbackExpression(this.shadowColor, other.shadowColor);

    if (other.shadowColorOpacity != null)
      this.shadowColorOpacity = makeFallbackExpression(this.shadowColorOpacity, other.shadowColorOpacity);

    if (other.font != null)
      this.font = makeFallbackExpression(this.font, other.font);

    if (other.reset != null)
      this.reset = makeFallbackExpression(this.reset, other.reset);

    if (other.formatStates != null) {
      for (Format format : Format.VALUES) {
        ExpressionNode otherValue = other.getFormat(format);

        if (otherValue != null)
          setFormat(format, makeFallbackExpression(getFormat(format), otherValue));
      }
    }
  }

  private ExpressionNode makeFallbackExpression(@Nullable ExpressionNode thisValue, ExpressionNode otherValue) {
    if (thisValue == null)
      return otherValue;

    return new InfixOperationNode(thisValue, new InfixOperatorToken(InputView.EMPTY, InfixOperator.FALLBACK), otherValue, null);
  }

  private @Nullable ExpressionNode applyCondition(@Nullable ExpressionNode expressionNode, @Nullable ExpressionNode condition) {
    if (condition == null)
      return expressionNode;

    if (expressionNode == null)
      return null;

    return new BranchingNode(condition, null, expressionNode, null, null);
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

  public void bakeUseCondition(ExpressionNode condition) {
    if (condition == null)
      throw new IllegalStateException("Cannot bake a null-condition");

    this.color = applyCondition(this.color, condition);
    this.shadowColor = applyCondition(this.shadowColor, condition);
    this.shadowColorOpacity = applyCondition(this.shadowColorOpacity, condition);
    this.font = applyCondition(this.font, condition);
    this.reset = applyCondition(this.reset, condition);

    for (Format format : Format.VALUES)
      setFormat(format, applyCondition(getFormat(format), condition));
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
