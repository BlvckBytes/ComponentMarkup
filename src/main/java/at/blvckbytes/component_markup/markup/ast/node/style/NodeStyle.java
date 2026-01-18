/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.style;

import at.blvckbytes.component_markup.expression.ast.BranchingNode;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.expression.tokenizer.token.NullToken;
import org.jetbrains.annotations.Nullable;

public class NodeStyle {

  private @Nullable ExpressionNode @Nullable [] formatStates;

  public @Nullable ExpressionNode color;
  public @Nullable ExpressionNode shadowColor;
  public @Nullable ExpressionNode shadowColorOpacity;
  public @Nullable ExpressionNode font;
  public @Nullable ExpressionNode reset;

  public void inheritFrom(NodeStyle other) {
    ExpressionNode otherValue;

    if ((otherValue = other.color) != null) {
      if (this.color == null)
        this.color = otherValue;
      else
        trySetFalseBranch(this.color, otherValue);
    }

    if ((otherValue = other.shadowColor) != null) {
      if (this.shadowColor == null)
        this.shadowColor = otherValue;
      else
        trySetFalseBranch(this.shadowColor, otherValue);
    }

    if ((otherValue = other.shadowColorOpacity) != null) {
      if (this.shadowColorOpacity == null)
        this.shadowColorOpacity = otherValue;
      else
        trySetFalseBranch(this.shadowColorOpacity, otherValue);
    }

    if ((otherValue = other.font) != null) {
      if (this.font == null)
        this.font = otherValue;
      else
        trySetFalseBranch(this.font, otherValue);
    }

    if ((otherValue = other.reset) != null) {
      if (this.reset == null)
        this.reset = otherValue;
      else
        trySetFalseBranch(this.font, otherValue);
    }

    if (other.formatStates != null) {
      for (Format format : Format.VALUES) {
        otherValue = other.getFormat(format);

        if (otherValue == null)
          continue;

        ExpressionNode thisFormatState = getFormat(format);

        if (thisFormatState == null) {
          setFormat(format, otherValue);
          continue;
        }

        trySetFalseBranch(thisFormatState, otherValue);
      }
    }
  }

  private @Nullable ExpressionNode applyCondition(@Nullable ExpressionNode expressionNode, @Nullable ExpressionNode condition) {
    if (condition == null)
      return expressionNode;

    if (expressionNode == null)
      return null;

    return new BranchingNode(condition, null, expressionNode, null, null);
  }

  private void trySetFalseBranch(ExpressionNode target, ExpressionNode expressionNode) {
    if (!(target instanceof BranchingNode))
      return;

    BranchingNode branchingNode = (BranchingNode) target;

    if (branchingNode.branchFalse == null) {
      branchingNode.branchFalse = expressionNode;
      return;
    }

    if (!(branchingNode.branchFalse instanceof TerminalNode))
      return;

    TerminalNode falseTerminal = (TerminalNode) branchingNode.branchFalse;

    if (!(falseTerminal.token instanceof NullToken))
      return;

    branchingNode.branchFalse = expressionNode;
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
