/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.player_name;

import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;

public class PlayerNameParameter implements RendererParameter {

  public boolean displayName;

  public PlayerNameParameter(boolean displayName) {
    this.displayName = displayName;
  }

  @Override
  public String asPlainText() {
    return toString();
  }

  @Override
  public String toString() {
    return "PlayerNameParameter{displayName=" + displayName + "}";
  }
}
