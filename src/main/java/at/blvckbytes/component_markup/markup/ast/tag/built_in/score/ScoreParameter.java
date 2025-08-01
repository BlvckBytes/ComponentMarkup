/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.score;

import at.blvckbytes.component_markup.markup.ast.node.terminal.RendererParameter;
import org.jetbrains.annotations.Nullable;

public class ScoreParameter implements RendererParameter {

  public final String name;
  public final String objective;
  public final @Nullable String value;

  public ScoreParameter(String name, String objective, @Nullable String value) {
    this.name = name;
    this.objective = objective;
    this.value = value;
  }

  @Override
  public String asPlainText() {
    return toString();
  }

  @Override
  public String toString() {
    return "ScoreParameter{" +
      "name='" + name + '\'' +
      ", objective='" + objective + '\'' +
      ", value='" + value + '\'' +
      '}';
  }
}
