/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.platform.selector.argument.ArgumentEntry;
import at.blvckbytes.component_markup.util.InputView;

import java.util.List;

public class TargetSelector {

  public final TargetType target;
  public final InputView rawTarget;
  public final List<ArgumentEntry> arguments;

  public TargetSelector(TargetType target, InputView rawTarget, List<ArgumentEntry> arguments) {
    this.target = target;
    this.rawTarget = rawTarget;
    this.arguments = arguments;
  }
}
