/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class DeferredNode<Parameter extends RendererParameter> extends UnitNode implements DeferredRenderer<Parameter> {

  public DeferredNode(InputView positionProvider, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(positionProvider, letBindings);
  }

  public abstract Parameter createParameter(Interpreter interpreter);

  // TODO: Add this method
  // public abstract boolean requiresRecipient();

}
