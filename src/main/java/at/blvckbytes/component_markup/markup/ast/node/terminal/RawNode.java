/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.util.InputView;

public class RawNode extends TerminalNode {

  public final Object value;

  public RawNode(Object value) {
    super(InputView.EMPTY, null);

    this.value = value;
  }
}
