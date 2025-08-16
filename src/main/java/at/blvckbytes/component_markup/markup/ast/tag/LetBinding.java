/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.util.InputView;

import java.util.Objects;

public abstract class LetBinding {

  public final InputView name;
  public final String bindingName;

  public LetBinding(InputView name) {
    this.name = name;
    this.bindingName = name.buildString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LetBinding)) return false;
    LetBinding that = (LetBinding) o;
    return Objects.equals(bindingName, that.bindingName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(bindingName);
  }
}
