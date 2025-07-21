package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class CombinationResult {

  public static final CombinationResult NO_OP_SENTINEL = new CombinationResult(null, null, null);

  public final Object component;
  public final @Nullable ComputedStyle styleToApply;
  public final @Nullable EnumMap<MembersSlot, AddressTree> deferredAddresses;

  public CombinationResult(
    Object component,
    @Nullable ComputedStyle styleToApply,
    @Nullable EnumMap<MembersSlot, AddressTree> deferredAddresses
  ) {
    this.component = component;
    this.styleToApply = styleToApply;
    this.deferredAddresses = deferredAddresses;
  }

  public static CombinationResult empty(ComponentConstructor componentConstructor) {
    return new CombinationResult(componentConstructor.createTextComponent(""), null, null);
  }
}
