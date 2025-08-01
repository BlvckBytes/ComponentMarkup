/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.selector;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.node.terminal.DeferredNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.interpreter.ComponentConstructor;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.markup.interpreter.SlotContext;
import at.blvckbytes.component_markup.markup.interpreter.SlotType;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

public class SelectorNode extends DeferredNode<SelectorParameter> {

  public final ExpressionNode selector;
  public final @Nullable MarkupNode separator;

  public SelectorNode(
    ExpressionNode selector,
    @Nullable MarkupNode separator,
    StringView positionProvider,
    @Nullable LinkedHashSet<LetBinding> letBindings
  ) {
    super(positionProvider, letBindings);

    this.selector = selector;
    this.separator = separator;
  }

  @Override
  public @Nullable List<Object> renderComponent(
    SelectorParameter selectorParameter,
    ComponentConstructor componentConstructor,
    InterpretationEnvironment environment,
    SlotContext slotContext,
    @Nullable Object recipient
  ) {
    // TODO: Implement
    throw new UnsupportedOperationException();
  }

  @Override
  public SelectorParameter createParameter(Interpreter interpreter) {
    String selector = interpreter.evaluateAsString(this.selector);

    Object separator = null;

    if (this.separator != null) {
      List<Object> components = interpreter.interpretSubtree(
        this.separator,
        interpreter.getComponentConstructor().getSlotContext(SlotType.SINGLE_LINE_CHAT)
      ).unprocessedComponents;

      separator = components.isEmpty() ? null : components.get(0);
    }

    return new SelectorParameter(selector, separator);
  }
}
