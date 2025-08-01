/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.platform.selector;

import at.blvckbytes.component_markup.platform.selector.argument.ArgumentEntry;
import at.blvckbytes.component_markup.util.StringView;

import java.util.List;

public class TargetSelector {

  public final TargetType target;
  public final StringView rawTarget;
  public final List<ArgumentEntry> arguments;

  public TargetSelector(TargetType target, StringView rawTarget, List<ArgumentEntry> arguments) {
    this.target = target;
    this.rawTarget = rawTarget;
    this.arguments = arguments;
  }
}
