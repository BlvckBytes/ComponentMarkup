/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public class ForLoopNode extends MarkupNode {

  public final InputView forAttribute;
  public final ExpressionNode iterable;
  public final @Nullable String iterationVariable;
  public final MarkupNode body;
  public final @Nullable MarkupNode separator;
  public final @Nullable MarkupNode empty;
  public final @Nullable ExpressionNode reversed;

  public @Nullable LinkedHashSet<LetBinding> letBindingsBeforeForAttribute;
  public @Nullable LinkedHashSet<LetBinding> letBindingsAfterForAttribute;

  public ForLoopNode(
    InputView forAttribute,
    ExpressionNode iterable,
    @Nullable InputView iterationVariable,
    MarkupNode body,
    @Nullable MarkupNode separator,
    @Nullable MarkupNode empty,
    @Nullable ExpressionNode reversed,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(iterable.getFirstMemberPositionProvider(), null, letBindings);

    this.forAttribute = forAttribute;
    this.iterable = iterable;
    this.iterationVariable = iterationVariable == null ? null : iterationVariable.buildString();
    this.body = body;
    this.separator = separator;
    this.empty = empty;
    this.reversed = reversed;

    if (letBindings == null)
      return;

    int forAttributePosition = forAttribute.getPosition();

    for (LetBinding letBinding : letBindings) {
      int bindingPosition = letBinding.name.getPosition();

      if (bindingPosition < forAttributePosition) {
        if (letBindingsBeforeForAttribute == null)
          letBindingsBeforeForAttribute = new LinkedHashSet<>();

        letBindingsBeforeForAttribute.add(letBinding);
        continue;
      }

      if (letBindingsAfterForAttribute == null)
        letBindingsAfterForAttribute = new LinkedHashSet<>();

      letBindingsAfterForAttribute.add(letBinding);
    }
  }

  @Override
  protected void inheritLetBindings(LinkedHashSet<LetBinding> letBindings) {
    if (this.letBindingsBeforeForAttribute == null) {
      this.letBindingsBeforeForAttribute = letBindings;
      return;
    }

    LinkedHashSet<LetBinding> totalBindings = new LinkedHashSet<>();
    totalBindings.addAll(letBindings);
    totalBindings.addAll(this.letBindingsBeforeForAttribute);

    this.letBindingsBeforeForAttribute = totalBindings;
  }
}
