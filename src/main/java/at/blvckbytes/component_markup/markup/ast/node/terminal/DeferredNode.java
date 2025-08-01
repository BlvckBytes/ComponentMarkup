/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class DeferredNode<Parameter extends RendererParameter> extends UnitNode implements DeferredRenderer<Parameter> {

  public DeferredNode(StringView positionProvider, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(positionProvider, letBindings);
  }

  public abstract Parameter createParameter(Interpreter interpreter);

}
