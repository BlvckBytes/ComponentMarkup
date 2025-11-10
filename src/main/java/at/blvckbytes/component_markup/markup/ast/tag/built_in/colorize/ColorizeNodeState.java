/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.interpreter.Interpreter;
import at.blvckbytes.component_markup.platform.ComponentConstructor;
import at.blvckbytes.component_markup.util.color.PackedColor;
import at.blvckbytes.component_markup.util.InputView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

public abstract class ColorizeNodeState {

  public final InputView tagName;
  public final double phase;
  public final EnumSet<ColorizeFlag> flags;
  public final int initialSubtreeDepth;

  private final Stack<List<Object>> injectedComponentsStack;

  public ColorizeNodeState(InputView tagName, int initialSubtreeDepth, double phase, EnumSet<ColorizeFlag> flags) {
    this.tagName = tagName;
    this.initialSubtreeDepth = initialSubtreeDepth;
    this.phase = phase;
    this.flags = flags;

    this.injectedComponentsStack = new Stack<>();
  }

  protected long getPackedColor(int index, int length) {
    double progressionPercentage = (index / (double) length) * 100;

    if (progressionPercentage > 100)
      progressionPercentage = 100;

    progressionPercentage += phase;

    while (progressionPercentage > 100)
      progressionPercentage -= 100;

    return getPackedColor(progressionPercentage);
  }

  protected abstract long getPackedColor(double progressionPercentage);

  public boolean doesTargetNode(ColorizeNode node) {
    return node.tagName.contentEquals(this.tagName.buildString(), true);
  }

  public void begin() {
    if (flags.contains(ColorizeFlag.MERGE_INNER) && !injectedComponentsStack.empty()) {
      injectedComponentsStack.push(null);
      return;
    }

    injectedComponentsStack.push(new ArrayList<>());
  }

  public void discard() {
    injectedComponentsStack.pop();
  }

  public boolean endAndGetIfStackIsEmpty(Interpreter interpreter) {
    List<Object> injectedComponents = injectedComponentsStack.pop();

    if (injectedComponents == null)
      return injectedComponentsStack.empty();

    ComponentConstructor componentConstructor = interpreter.getComponentConstructor();

    int length = injectedComponents.size();

    for (int index = 0; index < length; ++index) {
      Object injectedComponent = injectedComponents.get(index);

      long color = getPackedColor(index, length);

      if (color != PackedColor.NULL_SENTINEL)
        componentConstructor.setColor(injectedComponent, color);
    }

    return injectedComponentsStack.empty();
  }

  public void addInjected(Object component) {
    if (flags.contains(ColorizeFlag.MERGE_INNER)) {
      injectedComponentsStack.get(0).add(component);
      return;
    }

    injectedComponentsStack.peek().add(component);
  }
}
