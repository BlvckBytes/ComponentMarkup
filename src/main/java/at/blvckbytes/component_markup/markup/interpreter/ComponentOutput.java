package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ComponentOutput {

  public final List<Object> unprocessedComponents;
  public final @Nullable AddressTree deferredAddresses;
  private final ComponentConstructor componentConstructor;

  public ComponentOutput(
    List<Object> unprocessedComponents,
    @Nullable AddressTree deferredAddresses,
    ComponentConstructor componentConstructor
  ) {
    this.unprocessedComponents = unprocessedComponents;
    this.deferredAddresses = deferredAddresses;
    this.componentConstructor = componentConstructor;
  }

  public List<Object> get(@Nullable Object recipient) {
    List<Object> result = _substituteDeferred(unprocessedComponents, recipient);
    return result == null ? unprocessedComponents : result;
  }

  public @Nullable List<Object> _substituteDeferred(List<Object> components, @Nullable Object recipient) {
    List<Object> result = null;
    int resultOffset = 0;

    for (int componentIndex = 0; componentIndex < components.size(); ++componentIndex) {
      Object component = components.get(componentIndex);

      if (component instanceof DeferredComponent) {
        List<Object> renderedComponents = ((DeferredComponent) component).renderDeferredComponent(recipient);

        if (result == null)
          result = new ArrayList<>(components);

        if (renderedComponents == null || renderedComponents.isEmpty()) {
          result.remove(componentIndex + resultOffset);
          --resultOffset;
          continue;
        }

        // TODO: Support multi-component results
        result.set(componentIndex + resultOffset, renderedComponents.get(0));
        continue;
      }

      boolean isRefAltered = false;

      // If a deferred component has already been rendered, it itself will never emit (if well-behaving)
      // more deferred components, because the renderer is now aware of the recipient and can resolve
      // deferred nodes within the sub-AST (if applicable) immediately.
      for (MembersSlot slot : MembersSlot.VALUES) {
        List<Object> members = componentConstructor.getMembers(component, slot);

        if (members == null)
          continue;

        List<Object> slotResult = _substituteDeferred(members, recipient);

        if (slotResult == null)
          continue;

        if (!isRefAltered) {
          component = componentConstructor.shallowCopyIncludingMemberLists(component);
          isRefAltered = true;
        }

        Object setMembersResult = componentConstructor.setMembers(component, slot, slotResult);

        if (setMembersResult == null)
          LoggerProvider.log(Level.WARNING, "Could not write back members for slot " + slot);
        else
          component = setMembersResult;
      }

      if (isRefAltered) {
        if (result == null)
          result = new ArrayList<>(components);

        result.set(componentIndex + resultOffset, component);
      }
    }

    return result;
  }
}
