package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

public class CombinationResult {

  public final Object component;
  public final @Nullable ComputedStyle styleToApply;

  public CombinationResult(Object component, @Nullable ComputedStyle styleToApply) {
    this.component = component;
    this.styleToApply = styleToApply;
  }
}
