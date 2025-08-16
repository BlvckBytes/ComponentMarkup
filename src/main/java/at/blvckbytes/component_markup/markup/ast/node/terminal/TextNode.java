/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.node.terminal;

import at.blvckbytes.component_markup.util.InputView;

public class TextNode extends TerminalNode {

  public final String textValue;

  public TextNode(InputView positionProvider, String textValue) {
    super(positionProvider, null);

    this.textValue = textValue;
  }
}
