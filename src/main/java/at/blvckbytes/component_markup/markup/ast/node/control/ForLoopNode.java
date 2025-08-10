/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ForLoopNode extends MarkupNode {

  public final StringView forAttribute;
  public final ExpressionNode iterable;
  public final @Nullable String iterationVariable;
  public final MarkupNode body;
  public final @Nullable MarkupNode separator;
  public final @Nullable MarkupNode empty;
  public final @Nullable ExpressionNode reversed;

  public @Nullable List<LetBinding> letBindingsBeforeForAttribute;
  public @Nullable List<LetBinding> letBindingsAfterForAttribute;

  public ForLoopNode(
    StringView forAttribute,
    ExpressionNode iterable,
    @Nullable StringView iterationVariable,
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
          letBindingsBeforeForAttribute = new ArrayList<>();

        letBindingsBeforeForAttribute.add(letBinding);
        continue;
      }

      if (letBindingsAfterForAttribute == null)
        letBindingsAfterForAttribute = new ArrayList<>();

      letBindingsAfterForAttribute.add(letBinding);
    }
  }
}
