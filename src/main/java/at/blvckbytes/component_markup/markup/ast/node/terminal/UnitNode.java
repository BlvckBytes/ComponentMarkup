/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class UnitNode extends TerminalNode {

  public UnitNode(InputView positionProvider, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(positionProvider, letBindings);
  }
}
