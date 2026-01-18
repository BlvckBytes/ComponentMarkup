/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.ast.InfixOperationNode;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.markup.ast.node.style.NodeStyle;
import at.blvckbytes.component_markup.markup.ast.tag.ExpressionLetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupLetBinding;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public abstract class MarkupNode {

  private @Nullable ExpressionNode ifCondition;
  private @Nullable ExpressionNode useCondition;

  public final InputView positionProvider;

  public @Nullable List<MarkupNode> children;
  public @Nullable LinkedHashSet<LetBinding> letBindings;

  public MarkupNode(
    InputView positionProvider,
    @Nullable List<MarkupNode> children,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    this.positionProvider = positionProvider;
    this.children = children;
    this.letBindings = letBindings;
  }

  public @Nullable ExpressionNode getIfCondition() {
    return ifCondition;
  }

  public void setIfCondition(@Nullable ExpressionNode ifCondition) {
    this.ifCondition = ifCondition;
  }

  public @Nullable ExpressionNode getUseCondition() {
    return useCondition;
  }

  public void setUseCondition(@Nullable ExpressionNode useCondition) {
    if (this instanceof StyledNode) {
      if (useCondition == null || this.useCondition != null)
        throw new IllegalStateException("For style-nodes, use-condition must only be set once, to a non-null value");

      NodeStyle style = ((StyledNode) this).getStyle();

      if (style != null)
        style.bakeUseCondition(useCondition);
    }

    this.useCondition = useCondition;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean canBeUnpackedFromAndIfSoInherit(MarkupNode other) {
    // Do not add additional bindings (which would not be included) to a capture
    if (other.letBindings != null) {
      for (LetBinding letBinding : other.letBindings) {
        if (letBinding instanceof MarkupLetBinding && ((MarkupLetBinding) letBinding).capture)
          return false;

        if (letBinding instanceof ExpressionLetBinding && ((ExpressionLetBinding) letBinding).capture)
          return false;
      }
    }

    if (other instanceof StyledNode) {
      NodeStyle otherStyle = ((StyledNode) other).getStyle();

      if (otherStyle != null && otherStyle.hasNonNullProperties()) {
        if (!(this instanceof StyledNode))
          return false;

        ((StyledNode) this).getOrInstantiateStyle().inheritFrom(otherStyle);
      }
    }

    if (other.ifCondition != null) {
      if (this.ifCondition == null)
        this.ifCondition = other.ifCondition;
      else
        this.ifCondition = new InfixOperationNode(this.ifCondition, new InfixOperatorToken(InputView.EMPTY, InfixOperator.CONJUNCTION), other.ifCondition, null);
    }

    if (other.letBindings != null && !other.letBindings.isEmpty()) {
      if (this.letBindings == null)
        this.letBindings = other.letBindings;
      else {
        // Sadly, Java 8 does not support reverse iteration
        LinkedHashSet<LetBinding> totalBindings = new LinkedHashSet<>();
        totalBindings.addAll(other.letBindings);
        totalBindings.addAll(this.letBindings);
        this.letBindings = totalBindings;
      }
    }

    return true;
  }
}
