/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.markup.ast.node.StyledNode;
import at.blvckbytes.component_markup.markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.markup.parser.ParserChildItem;
import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class TerminalNode extends StyledNode implements ParserChildItem {

  public TerminalNode(InputView positionProvider, @Nullable LinkedHashSet<LetBinding> letBindings) {
    super(positionProvider, null, letBindings);
  }
}
